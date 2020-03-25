package client;

public interface AsyncRPCCallback {
    void success(Object obj);

    void fail(Exception e);
}
