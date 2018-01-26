package org.seckill.enums;

/**
 * User: chanson-pro
 * Date-Time: 2018-1-25 18:26
 * Description:
 */
public enum SeckillStatEnum {

    SUCCEESS(1,"秒杀成功"),
    END(0,"秒杀结束"),
    REPEAT_KILL(-1,"重复秒杀"),
    INNER_ERROR(-2,"系统异常"),
    DATE_REWRITE(-3,"数据篡改");

    private int state;
    private String info;

    SeckillStatEnum(int state, String info) {
        this.state = state;
        this.info = info;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public static SeckillStatEnum stateOf(int index){
        for (SeckillStatEnum state : values())
        {
            if (state.getState()==index)
            {
                return state;
            }
        }
        return null;
    }
}
