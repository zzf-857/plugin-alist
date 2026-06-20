package run.halo.alist.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * 2024/8/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AListGetCurrentUserInfoRes {
    private int id;
    private String username;
    private String password;
    /**
     * 基本路径
     */
    @JsonProperty("base_path")
    private String basePath;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Integer> role;
    private boolean disabled;
    private int permission;
    @JsonProperty("sso_id")
    private String ssoId;
    /**
     * 是否开启二步验证
     */
    private boolean otp;
}
