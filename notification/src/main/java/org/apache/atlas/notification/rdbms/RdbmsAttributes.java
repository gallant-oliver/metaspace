package org.apache.atlas.notification.rdbms;

public class RdbmsAttributes {
    private String typeName;
    private InnerAttributes uniqueAttributes;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public InnerAttributes getUniqueAttributes() {
        return uniqueAttributes;
    }

    public void setUniqueAttributes(InnerAttributes uniqueAttributes) {
        this.uniqueAttributes = uniqueAttributes;
    }

    static class InnerAttributes{
        private String qualifiedName;

        public String getQualifiedName() {
            return qualifiedName;
        }

        public void setQualifiedName(String qualifiedName) {
            this.qualifiedName = qualifiedName;
        }
    }
}
