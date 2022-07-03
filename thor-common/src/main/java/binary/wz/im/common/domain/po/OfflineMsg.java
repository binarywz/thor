package binary.wz.im.common.domain.po;

/**
 * @author binarywz
 * @date 2022/6/1 23:03
 * @description: TODO 离线消息后续迁移至HBase
 */
public class OfflineMsg extends DbModel {

    private String msgId;

    private Long seq;

    private Integer msgCode;

    private String toUserId;

    private byte[] content;

    private Boolean hasRead;

    public Boolean getHasRead() {
        return hasRead;
    }

    public void setHasRead(Boolean hasRead) {
        this.hasRead = hasRead;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Integer getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(Integer msgCode) {
        this.msgCode = msgCode;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }
}
