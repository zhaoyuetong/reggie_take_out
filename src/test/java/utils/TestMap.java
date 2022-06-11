package utils;

import entity.Result;
import org.junit.Test;

public class TestMap {
    @Test
    public void test(){
        GaoDeMapUtil gaoDeMapUtil = new GaoDeMapUtil();
        float distance = gaoDeMapUtil.getDistanceByAddress("山东省济南市岔路街", "山东省滕州市张汪镇大苏庄村");
        System.out.println(distance/1000.0+"公里");
    }
}
