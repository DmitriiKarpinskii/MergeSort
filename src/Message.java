public class Message {
    private boolean status;
    private String message;

    Message(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    boolean getStatus() {
        return status;
    }
    void setStatus(boolean ok) {
        status = ok;
    }

    void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}
