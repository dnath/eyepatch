package com.appfolio.eyepatch.model;

import java.io.Serializable;
import java.util.List;

public interface Inspectable extends Serializable {
    public String getAuthToken();
    public String getCookie();
    public String getName();
    public int indexInParent();
    public int getId();
    public boolean isDoneBeingInspected();
    public List<? extends Inspectable> getChildren();
    public List<? extends Inspectable> getSiblings();
    public Inspectable getParent();
    public Inspectable next();
    public Inspectable previous();
}
