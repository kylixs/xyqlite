package com.androidxyq.scene;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: gongdewei
 * Date: 12-3-25
 * Time: 下午7:56
 */
public interface NpcDialogueHandler {

    /**
     * 判断是否支持某个场景
     * @param sceneId  场景id
     * @return
     */
    boolean support(String sceneId, String npcName);

    /**
     * 获取NPC的对话内容
     * @return
     */
    String[] getChoices(String sceneId, String npcName);

    /**
     * 用户选择后触发此方法
     * @param scene
     * @param npcName
     * @param choiceIndex
     * @param choice
     */
    void onChoice(SceneScreen scene, String npcName, int choiceIndex, String choice);

    void onMove(SceneScreen scene, int x, int y);
}
