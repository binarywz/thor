package binary.wz.im.client.service;

import binary.wz.im.client.domain.vo.UserReq;
import binary.wz.im.common.domain.ResultWrapper;
import binary.wz.im.common.domain.UserInfo;
import binary.wz.im.common.domain.po.RelationDetail;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * @author binarywz
 * @date 2022/6/11 17:54
 * @description:
 */
public interface RestService {
    /**
     * 登录
     * @param userReq
     * @return
     */
    @Headers("Content-Type: application/json")
    @POST("/user/login")
    Call<ResultWrapper<UserInfo>> login(@Body UserReq userReq);

    /**
     * 注销
     * @param token
     * @return
     */
    @POST("/user/logout")
    Call<ResultWrapper<Void>> logout(@Header("token") String token);

    /**
     * 获取好友列表
     * @param userId
     * @param token
     * @return
     */
    @GET("/relation/{id}")
    Call<ResultWrapper<List<RelationDetail>>> friends(@Path("id") String userId, @Header("token") String token);

    @GET("/relation")
    Call<ResultWrapper<RelationDetail>> relation(
            @Query("userId1") String userId1, @Query("userId2") String userId2,
            @Header("token") String token);
}
