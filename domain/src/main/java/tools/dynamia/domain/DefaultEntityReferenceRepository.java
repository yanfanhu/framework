/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tools.dynamia.domain;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.collect.PagedList;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.CrudServiceListenerAdapter;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default {@link EntityReferenceRepository} with cache support
 *
 * @param <ID>
 */
public class DefaultEntityReferenceRepository<ID extends Serializable> extends CrudServiceListenerAdapter implements EntityReferenceRepository<ID> {

    private LoggingService logger = new SLF4JLoggingService(DefaultEntityReferenceRepository.class);

    private CrudService crudService;
    private Class<?> entityClass;
    private int maxResult = 60;
    private String[] findFields;
    private boolean cacheable;
    private String cacheName;

    public DefaultEntityReferenceRepository(Class<?> entityClass) {
        this.entityClass = entityClass;
        initCacheName();
    }

    public DefaultEntityReferenceRepository(Class<?> entityClass, String... findFields) {
        super();
        this.entityClass = entityClass;
        this.findFields = findFields;
        initCacheName();
    }

    public DefaultEntityReferenceRepository(CrudService crudService, Class<?> entityClass, String... findFields) {
        super();
        this.crudService = crudService;
        this.entityClass = entityClass;
        this.findFields = findFields;
        initCacheName();
    }

    private void initCacheName() {
        cacheName = getAlias() + "RefCache";
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public void setFindFields(String... findFields) {
        this.findFields = findFields;
    }

    /**
     * Sets the cacheable. Only affects load(id) method, if you want cache for
     * find method, extends this class override find method and use protected
     * methods for cache handling
     *
     * @param cacheable the new cacheable
     */
    public void setCacheable(boolean cacheable) {
        this.cacheable = cacheable;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public String getAlias() {
        return entityClass.getSimpleName();
    }

    @Override
    public String getEntityClassName() {
        return entityClass.getName();
    }

    @Override
    public EntityReference<ID> load(ID id) {
        if (id == null) {
            return null;
        }

        EntityReference<ID> reference = loadFromCache(id, EntityReference.class);

        if (reference == null) {
            try {
                Object entity = getCrudService().find(entityClass, id);
                reference = createEntityReference(id, entity);
                if (reference != null) {
                    saveToCache(reference.getId(), reference);
                }
            } catch (Exception e) {
                logger.error("Error loading EntityReference id [" + id + "], entity class [" + getEntityClassName() + "]", e);
            }
        }

        return reference;
    }

    @Override
    public EntityReference<ID> load(String field, Object value) {
        Object entity = getCrudService().findSingle(entityClass, QueryParameters.with(field, value)
                .setAutocreateSearcheableStrings(false));

        if (entity != null) {
            ID id = getId(entity);
            return createEntityReference(id, entity);
        }

        return null;
    }

    @Override
    public EntityReference<ID> load(Map<String, Object> params) {
        QueryParameters qparms = new QueryParameters(params);
        qparms.setAutocreateSearcheableStrings(false);
        Object entity = getCrudService().findSingle(entityClass, qparms);

        if (entity != null) {
            ID id = getId(entity);
            return createEntityReference(id, entity);
        }

        return null;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<EntityReference<ID>> find(String text, Map<String, Object> params) {

        List<EntityReference<ID>> entityReferences = new ArrayList<>();
        QueryParameters queryParameters = new QueryParameters(params);

        if (findFields == null) {
            findFields = new String[]{"id"};
        }

        queryParameters.paginate(maxResult);
        List result = getCrudService().findByFields(entityClass, text, queryParameters, findFields);
        if (result instanceof PagedList) {
            result = ((PagedList) result).getDataSource().getPageData();
        }

        if (result != null) {
            for (Object entity : result) {
                ID id = getId(entity);

                EntityReference<ID> reference = createEntityReference(id, entity);
                if (reference != null) {
                    entityReferences.add(reference);
                }
            }
        }

        return entityReferences;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private ID getId(Object entity) {
        ID id = null;
        if (entity instanceof Identifiable) {
            id = (ID) ((Identifiable) entity).getId();
        } else {
            id = (ID) DomainUtils.findEntityId(entity);
        }
        return id;
    }

    @Override
    public EntityReference<ID> getFirst() {
        Object entity = getCrudService().findSingle(entityClass, new QueryParameters());
        if (entity != null) {
            return createEntityReference(getId(entity), entity);
        } else {
            return null;
        }
    }

    protected EntityReference<ID> createEntityReference(ID id, Object entity) {
        if (entity != null && id != null) {
            EntityReference ref = null;
            if (entity instanceof Referenceable) {
                ref = ((Referenceable<?>) entity).toEntityReference();
            } else {
                ref = new EntityReference<>(id, getEntityClassName(), entity.toString());
                if (entity instanceof Mappable) {
                    ref.getAttributes().putAll(((Mappable) entity).toMap());
                } else {
                    ref.getAttributes().putAll(BeanUtils.getValuesMaps(entity));
                }
            }
            return ref;
        } else {
            return null;
        }
    }

    public int getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    private CrudService getCrudService() {
        if (crudService == null) {
            crudService = Containers.get().findObject(CrudService.class);
        }
        return crudService;
    }

    protected void saveToCache(Object key, Object value) {
        Cache cache = getCache();
        if (cache != null) {
            cache.put(key, value);
        }
    }

    protected void removeFromCache(Object key) {
        Cache cache = getCache();
        if (cache != null) {
            cache.evict(key);
        }
    }

    protected <T> T loadFromCache(Object key, Class<T> clazz) {
        T value = null;

        Cache cache = getCache();
        if (cache != null) {
            value = cache.get(key, clazz);
        }

        return value;
    }

    protected Cache getCache() {
        if (isCacheable()) {
            CacheManager cacheManager = Containers.get().findObject(CacheManager.class);
            if (cacheManager != null) {
                Cache cache = cacheManager.getCache(cacheName);

                if (cache == null) {
                    logger.warn("Cacheable is active but cache with name [" + cacheName + "] is not found.  entity class ["
                            + getEntityClassName() + "]");
                }
                return cache;
            } else {
                logger.warn("CacheManager not found, make sure that a Spring cache manager is configured");
            }
        }
        return null;
    }

    @Override
    public void afterDelete(Object entity) {
        clearCache(entity);
    }

    @Override
    public void afterUpdate(Object entity) {
        clearCache(entity);
    }

    private void clearCache(Object entity) {
        if (entityClass != null && isCacheable() && entity != null && entity.getClass() == entityClass) {
            try {
                Object id = DomainUtils.findEntityId(entity);
                if (id != null) {
                    removeFromCache(id);
                }
            } catch (Exception e) {
                logger.error("Error clearing cache from EntityReferenceRepository - Entity: " + entityClass, e);
            }
        }
    }
}
