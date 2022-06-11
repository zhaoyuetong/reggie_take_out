package utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: 高德地图工具类
 * @Author: isymikasan
 * @Date: 2021-12-22 09:19:02
 */
@Component
@Slf4j
public class GaoDeMapUtil {

    /**
     * 功能描述: 高德地图Key
     *
     * @param null
     * @return
     * @author 赵月彤
     * @date 2022-01-26 09:13:40
     */
    private static final String GAO_DE_KEY = "8ff37c1a132be62e76edbf32de7c8f11";

    //申请的账户Key

    /**
     * 功能描述: 根据地址名称得到两个地址间的距离
     *
     * @param start 起始位置
     * @param end   结束位置
     * @return long 两个地址间的距离
     * @author isymikasan
     * @date 2022-01-26 09:16:04
     */
    public Long getDistanceByAddress(String start, String end) {
        String startLonLat = getLonLat(start).getDatas().toString();
        String endLonLat = getLonLat(end).getDatas().toString();
        Long distance = Long.valueOf(getDistance(startLonLat, endLonLat).getDatas().toString());
        return distance;
    }

    /**
     * 功能描述: 地址转换为经纬度
     *
     * @param address 地址
     * @return java.lang.String 经纬度
     * @author isymikasan
     * @date 2022-01-26 09:17:13
     */
    public Result getLonLat(String address) {
        try {
            // 返回输入地址address的经纬度信息, 格式是 经度,纬度
            String queryUrl = "http://restapi.amap.com/v3/geocode/geo?key=" + GAO_DE_KEY + "&address=" + address;
            // 高德接口返回的是JSON格式的字符串
            String queryResult = getResponse(queryUrl);
            JSONObject job = JSONObject.parseObject(queryResult);
            JSONObject jobJSON = JSONObject
                    .parseObject(
                            job.get("geocodes").toString().substring(1, job.get("geocodes").toString().length() - 1));
            String LngAndLat = jobJSON.get("location").toString();
            log.info("经纬度为：" + LngAndLat);
            return Result.succeed(LngAndLat, "经纬度转换成功！");
        } catch (Exception e) {
            return Result.failed(e.toString());
        }
    }

    /**
     * 将经纬度 转换为 地址
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @return 地址名称
     * @throws Exception
     */
    public Result getAddress(String longitude, String latitude) throws Exception {
        String url;
        try {
            url = "http://restapi.amap.com/v3/geocode/regeo?output=JSON&location=" + longitude + "," + latitude
                    + "&key=" + GAO_DE_KEY + "&radius=0&extensions=base";

            log.info("经度" + longitude);
            log.info("纬度：" + latitude);
            log.info("url:" + url);

            // 高德接口返回的是JSON格式的字符串
            String queryResult = getResponse(url);
            if (ObjectUtils.isNull(queryResult)) {
                return Result.failed("查询结果为空");
            }

            // 将获取结果转为json 数据
            JSONObject obj = JSONObject.parseObject(queryResult);
            if (obj.get(GaoDeEnum.STATUS.getCode()).toString().equals(GaoDeEnum.INT_ONE.getCode())) {
                // 如果没有返回-1
                JSONObject reGeoCode = obj.getJSONObject(GaoDeEnum.RE_GEO_CODE.getCode());
                if (reGeoCode.size() > 0) {
                    // 在regeocode中拿到 formatted_address 具体位置
                    String formatted = reGeoCode.get("formatted_address").toString();
                    return Result.succeed(formatted, "地址获取成功！");

                } else {
                    return Result.failed("未找到相匹配的地址！");
                }
            } else {
                return Result.failed("请求错误！");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Result.failed("系统未知异常，请稍后再试");
        }
    }

    /**
     * 功能描述: 根据两个定位点的经纬度算出两点间的距离
     *
     * @param startLonLat 起始经纬度
     * @param endLonLat   结束经纬度（目标经纬度）
     * @return java.lang.Long 两个定位点之间的距离
     * @author isymikasan
     * @date 2022-01-26 09:47:42
     */
    public Result getDistance(String startLonLat, String endLonLat) {
        try {
            // 返回起始地startAddr与目的地endAddr之间的距离，单位：米
            Long result = new Long(0);
            String queryUrl =
                    "http://restapi.amap.com/v3/distance?key=" + GAO_DE_KEY + "&origins=" + startLonLat
                            + "&destination="
                            + endLonLat;
            String queryResult = getResponse(queryUrl);
            JSONObject job = JSONObject.parseObject(queryResult);
            JSONArray ja = job.getJSONArray("results");
            JSONObject jobO = JSONObject.parseObject(ja.getString(0));
            result = Long.parseLong(jobO.get("distance").toString());
            log.info("距离：" + result);
            return Result.succeed(result, "距离计算成功！");
        } catch (Exception e) {
            return Result.failed(e.toString());
        }


    }

    /**
     * 功能描述: 发送请求
     *
     * @param serverUrl 请求地址
     * @return java.lang.String
     * @author isymikasan
     * @date 2022-01-26 09:15:01
     */
    private static String getResponse(String serverUrl) {
        // 用JAVA发起http请求，并返回json格式的结果
        StringBuffer result = new StringBuffer();
        try {
            URL url = new URL(serverUrl);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            in.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

}