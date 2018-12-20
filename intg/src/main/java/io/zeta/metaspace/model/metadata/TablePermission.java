package io.zeta.metaspace.model.metadata;

import java.io.Serializable;

public class TablePermission implements Serializable {
    //读
    private boolean READ ;
    //写
    private boolean WRITE ;

    public boolean isREAD() {
        return READ;
    }

    public void setREAD(boolean READ) {
        this.READ = READ;
    }

    public boolean isWRITE() {
        return WRITE;
    }

    public void setWRITE(boolean WRITE) {
        this.WRITE = WRITE;
    }

}
