package io.zeta.metaspace.model.metadata;

import java.io.Serializable;

public class TablePermission implements Serializable {
    //读
    private boolean read ;
    //写
    private boolean write ;

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

}
