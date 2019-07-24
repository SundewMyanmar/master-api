package com.sdm.master.request;

import com.sdm.master.entity.PermissionEntity;

/**
 * @author m0n-hash
 */
public class PermissionRequest extends PermissionEntity {
    public boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
