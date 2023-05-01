package tools.dynamia.actions;

import tools.dynamia.commons.MapBuilder;
import tools.dynamia.integration.Containers;

import java.util.HashMap;
import java.util.Map;

public class Actions {

    private static final String ACTION_PARAM = "action";

    /**
     * Helper method to run actions
     *
     * @param action
     * @param eventBuilder
     * @param source
     * @param params
     */
    public static void run(Action action, ActionEventBuilder eventBuilder, Object source, Object data, Map<String, Object> params) {
        if (action != null) {
            if (params == null) {
                params = MapBuilder.put(ACTION_PARAM, action);
            } else if (!params.containsKey(ACTION_PARAM)) {
                params = new HashMap<>(params);
                params.put(ACTION_PARAM, action);
            }

            var event = eventBuilder.buildActionEvent(source, params);
            if (data != null) {
                event.setData(data);
            }

            if (action instanceof ActionFilter) {
                ((ActionFilter) action).beforeActionPerformed(event);
            }

            if (event.isPropagatable()) {
                var actionRunner = Containers.get().findObject(ActionRunner.class);
                if (actionRunner == null) {
                    actionRunner = new DefaultActionRunner();
                }
                actionRunner.run(action, event);
            }

            if (event.isPropagatable() && action instanceof ActionFilter) {
                ((ActionFilter) action).afterActionPerformed(event);
            }
        }
    }

    public static void run(Action action, ActionEventBuilder eventBuilder, Object source, Map<String, Object> params) {
        run(action, eventBuilder, source, null, params);
    }

    public static void run(Action action, ActionEventBuilder eventBuilder, Object source, Object data) {
        run(action, eventBuilder, source, data, null);
    }

    /**
     * Run action without source and params
     *
     * @param action
     * @param eventBuilder
     */
    public static void run(Action action, ActionEventBuilder eventBuilder) {
        run(action, eventBuilder, null, null);
    }

    public static void run(Action action, ActionEventBuilder eventBuilder, Object source) {
        run(action, eventBuilder, source, null);
    }

    /**
     * Run action without event builder
     *
     * @param action
     */
    public static void run(Action action, Object data) {
        run(action, (source, params) -> new ActionEvent(data, source, params));
    }
}
