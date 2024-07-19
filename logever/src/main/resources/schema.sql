DROP TRIGGER IF EXISTS trg_audit_new_users ^;

CREATE TRIGGER trg_audit_new_users
AFTER INSERT ON user FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE INT;
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	SET V_ACTION = 'CREATED';
	SET V_PK_VALUE = NEW.id;
	SET V_TYPE = 'USER';
	SET V_PREV_STATE = null;
	SET V_USERNAME = NEW.created_by;
	SET V_NEW_STATE = concat('Username: ',NEW.username,' Email: ',NEW.email,' FirstName: ',NEW.first_name,' LastName: ',NEW.last_name,' Department: ',NEW.department_id,' IsActive: ',NEW.is_active);

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_update_users ^;

CREATE TRIGGER trg_audit_update_users
AFTER UPDATE ON user FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE INT;
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	declare V_CHECK INT;
	SET V_ACTION = 'UPDATED';
	SET V_PK_VALUE = NEW.id;
	SET V_TYPE = 'USER';
	SET V_USERNAME = NEW.updated_by;
	SET V_PREV_STATE = '';
	SET V_NEW_STATE = '';
	SET V_CHECK = 0;
	IF coalesce(NEW.first_name,'') <> coalesce(OLD.first_name,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'FirstName: ',OLD.first_name,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'FirstName: ',NEW.first_name,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.last_name,'') <> coalesce(OLD.last_name,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'LastName: ',OLD.last_name,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'LastName: ',NEW.last_name,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.department_id,'') <> coalesce(OLD.department_id,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Department: ',OLD.department_id,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Department: ',NEW.department_id,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.password,'') <> coalesce(OLD.password,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Password: ','XXXXXXXXXXX',' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Password: ','XXXXXXXXXXX',' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.code,'') <> coalesce(OLD.code,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Code: ',OLD.code,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Code: ',NEW.code,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.date_of_birth,'') <> coalesce(OLD.date_of_birth,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'DOB: ',OLD.date_of_birth,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'DOB: ',NEW.date_of_birth,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.designation,'') <> coalesce(OLD.designation,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Designation: ',OLD.designation,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Designation: ',NEW.designation,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.email,'') <> coalesce(OLD.email,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Email: ',OLD.email,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Email: ',NEW.email,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.employee_code,'') <> coalesce(OLD.employee_code,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'EmployeeCode: ',OLD.employee_code,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'EmployeeCode: ',NEW.employee_code,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.hire_date,'') <> coalesce(OLD.hire_date,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'HireDate: ',OLD.hire_date,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'HireDate: ',NEW.hire_date,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.reporting_manager,'') <> coalesce(OLD.reporting_manager,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Reporting Manager: ',OLD.reporting_manager,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Reporting Manager: ',NEW.reporting_manager,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.windows_id,'') <> coalesce(OLD.windows_id,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Windows ID: ',OLD.windows_id,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Windows ID: ',NEW.windows_id,' ');
	SET V_CHECK = 1;
	END IF;
	IF coalesce(NEW.is_active,'') <> coalesce(OLD.is_active,'') THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'IsActive: ',OLD.is_active,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'IsActive: ',NEW.is_active,' ');
	SET V_CHECK = 1;
	END IF;
	IF NEW.last_login_dt <> OLD.last_login_dt || OLD.last_login_dt is NULL THEN
	IF V_CHECK <> 1 THEN
	SET V_CHECK = 2;
	SET V_NEW_STATE = concat(V_NEW_STATE, NEW.username,' logged In');
	END IF;
	END IF;

	IF V_CHECK < 1 THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'User unchanged');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Role attached to User is changed');
	END IF;

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_create_roles ^;

CREATE TRIGGER trg_audit_create_roles
AFTER INSERT ON role FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE INT;
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	SET V_ACTION = 'CREATED';
	SET V_PK_VALUE = NEW.id;
	SET V_TYPE = 'ROLE';
	SET V_PREV_STATE = null;
	SET V_USERNAME = NEW.created_by;
	SET V_NEW_STATE = concat('Role: ',NEW.role,' Description: ',NEW.description);

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_update_roles ^;

CREATE TRIGGER trg_audit_update_roles
AFTER UPDATE ON role FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE INT;
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	declare V_CHECK INT;
	SET V_ACTION = 'UPDATED';
	SET V_PK_VALUE = NEW.id;
	SET V_TYPE = 'ROLE';
	SET V_USERNAME = NEW.updated_by;
	SET V_PREV_STATE = '';
	SET V_NEW_STATE = '';
	SET V_CHECK = 0;
	IF NEW.description <> OLD.description THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Description: ',OLD.description,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Description: ',NEW.description,' ');
	SET V_CHECK = 1;
	END IF;
	IF NEW.role <> OLD.role THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Role: ',OLD.role,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Role: ',NEW.role,' ');
	SET V_CHECK = 1;
	END IF;
	IF V_CHECK < 1 THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Role Unchanged');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Permission attached to Role are changed');
	END IF;
	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);

END; ^;

DROP TRIGGER IF EXISTS trg_audit_create_role_permissions ^;

CREATE TRIGGER trg_audit_create_role_permissions
AFTER INSERT ON role_permission FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE VARCHAR(255);
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	SET V_ACTION = 'CREATED';
	SET V_PK_VALUE = concat(NEW.role_id,"-",NEW.permission_id);
	SET V_TYPE = 'ROLE_PERMISSION';
	SET V_PREV_STATE = null;
	SET V_USERNAME = 'SYSTEM';

	select created_by into V_USERNAME from role where id = NEW.role_id;

	IF V_USERNAME is NULL THEN
	SET V_USERNAME = 'SYSTEM';
	END IF;

	SET V_NEW_STATE = concat('RoleId: ',NEW.role_id,' PermissionId: ',NEW.permission_id);

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_delete_role_permissions ^;

CREATE TRIGGER trg_audit_delete_role_permissions
AFTER DELETE ON role_permission FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE VARCHAR(255);
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	SET V_ACTION = 'DELETED';
	SET V_PK_VALUE = concat(OLD.role_id,"-",OLD.permission_id);
	SET V_TYPE = 'ROLE_PERMISSION';
	SET V_NEW_STATE = null;
	SET V_USERNAME = 'SYSTEM';

	select updated_by into V_USERNAME from role where id = OLD.role_id;

	IF V_USERNAME is NULL THEN
	SET V_USERNAME = 'SYSTEM';
	END IF;

	SET V_PREV_STATE = concat('RoleId: ',OLD.role_id,' PermissionId: ',OLD.permission_id);

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_create_user_roles ^;

CREATE TRIGGER trg_audit_create_user_roles
AFTER INSERT ON user_role FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE VARCHAR(255);
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	SET V_ACTION = 'CREATED';
	SET V_PK_VALUE = concat(NEW.user_id,"-",NEW.role_id);
	SET V_TYPE = 'USER_ROLE';
	SET V_PREV_STATE = null;
	SET V_USERNAME = 'SYSTEM';

	select created_by into V_USERNAME from user where id = NEW.user_id;

	IF V_USERNAME is NULL THEN
	SET V_USERNAME = 'SYSTEM';
	END IF;

	SET V_NEW_STATE = concat('UserId: ',NEW.user_id,' RoleId: ',NEW.role_id);

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_delete_user_roles ^;

CREATE TRIGGER trg_audit_delete_user_roles
AFTER DELETE ON user_role FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE VARCHAR(255);
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	SET V_ACTION = 'DELETED';
	SET V_PK_VALUE = concat(OLD.user_id,"-",OLD.role_id);
	SET V_TYPE = 'USER_ROLE';
	SET V_NEW_STATE = null;
	SET V_USERNAME = 'SYSTEM';

	select updated_by into V_USERNAME from user where id = OLD.user_id;

	IF V_USERNAME is NULL THEN
	SET V_USERNAME = 'SYSTEM';
	END IF;

	SET V_PREV_STATE = concat('UserId: ',OLD.user_id,' RoleId: ',OLD.role_id);

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_update_settings ^;

CREATE TRIGGER trg_audit_update_settings
AFTER UPDATE ON settings FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE INT;
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	declare V_CHECK INT;
	SET V_ACTION = 'UPDATED';
	SET V_PK_VALUE = NEW.id;
	SET V_TYPE = 'SETTING';
	SET V_USERNAME = NEW.updated_by;
	SET V_PREV_STATE = '';
	SET V_NEW_STATE = '';
	IF NEW.value <> OLD.value THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Key: ',OLD.key,' Value: ',OLD.value);
	SET V_NEW_STATE = concat(V_NEW_STATE,'Key: ',NEW.key,' Value: ',NEW.value);
	END IF;
	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);

END; ^;

DROP TRIGGER IF EXISTS trg_audit_create_forms ^;

CREATE TRIGGER trg_audit_create_forms
AFTER INSERT ON form FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE VARCHAR(255);
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	SET V_ACTION = 'CREATED';
	SET V_PK_VALUE = New.id;
	SET V_TYPE = 'FORM';
	SET V_PREV_STATE = null;
	SET V_USERNAME = New.created_by;

	IF V_USERNAME is NULL THEN
	SET V_USERNAME = 'SYSTEM';
	END IF;

	SET V_NEW_STATE = concat('Name: ',NEW.name);

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_update_forms ^;

CREATE TRIGGER trg_audit_update_forms
AFTER UPDATE ON form FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE INT;
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	declare V_CHECK INT;
	SET V_ACTION = 'UPDATED';
	SET V_PK_VALUE = NEW.id;
	SET V_TYPE = 'FORM';
	SET V_USERNAME = NEW.updated_by;
	SET V_PREV_STATE = '';
	SET V_NEW_STATE = '';
	SET V_CHECK = 0;
	IF NEW.name <> OLD.name THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Name: ',OLD.name,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Name: ',NEW.name,' ');
	SET V_CHECK = 1;
	END IF;
	IF NEW.columns <> OLD.columns THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Columns: ',OLD.columns,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Columns: ',NEW.columns,' ');
	SET V_CHECK = 1;
	END IF;
    IF NEW.template <> OLD.template THEN
    IF V_CHECK = 1 THEN
    SET V_PREV_STATE = concat(V_PREV_STATE,'& Template updated');
    SET V_NEW_STATE = concat(V_NEW_STATE,'& Template updated');
    ELSE
    SET V_PREV_STATE = concat(V_PREV_STATE,'Template updated');
    SET V_NEW_STATE = concat(V_NEW_STATE,'Template updated');
    END IF;
    END IF;
	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);

END; ^;

DROP TRIGGER IF EXISTS trg_audit_create_depts ^;

CREATE TRIGGER trg_audit_create_depts
AFTER INSERT ON department FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE VARCHAR(255);
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	SET V_ACTION = 'CREATED';
	SET V_PK_VALUE = New.id;
	SET V_TYPE = 'DEPARTMENT';
	SET V_PREV_STATE = null;
	SET V_USERNAME = null;
    SET V_NEW_STATE = '';

	IF V_USERNAME is NULL THEN
	SET V_USERNAME = 'ADMIN';
	END IF;

	SET V_NEW_STATE = concat(V_NEW_STATE,'Name: ',NEW.name,' Code: ',NEW.code,' ParentID: ',NEW.parent_id,' Site: ',coalesce(NEW.site,'NULL'));

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);
END; ^;

DROP TRIGGER IF EXISTS trg_audit_update_depts ^;

CREATE TRIGGER trg_audit_update_depts
AFTER UPDATE ON department FOR EACH ROW
BEGIN
	declare V_ACTION VARCHAR(255);
	declare V_PK_VALUE INT;
	declare V_TYPE VARCHAR(255);
	declare V_PREV_STATE TEXT;
	declare V_USERNAME VARCHAR(255);
	declare V_NEW_STATE TEXT;
	declare V_CHECK INT;
	SET V_ACTION = 'UPDATED';
	SET V_PK_VALUE = NEW.id;
	SET V_TYPE = 'DEPARTMENT';
	SET V_USERNAME = 'ADMIN';
	SET V_PREV_STATE = '';
	SET V_NEW_STATE = '';
	SET V_CHECK = 0;
	IF NEW.name <> OLD.name THEN
	SET V_PREV_STATE = concat(V_PREV_STATE,'Name: ',OLD.name,' ');
	SET V_NEW_STATE = concat(V_NEW_STATE,'Name: ',NEW.name,' ');
	SET V_CHECK = 1;
	END IF;
    IF coalesce(NEW.site,'NULL') <> coalesce(OLD.site,'NULL') THEN
    SET V_PREV_STATE = concat(V_PREV_STATE,' Site updated: ',coalesce(OLD.site,'NULL'));
    SET V_NEW_STATE = concat(V_NEW_STATE,' Site updated: ',coalesce(NEW.site,'NULL'));
    SET V_CHECK = 1;
    END IF;
    IF NEW.parent_id <> OLD.parent_id THEN
    SET V_PREV_STATE = concat(V_PREV_STATE,' ParentID updated: ',OLD.parent_id);
    SET V_NEW_STATE = concat(V_NEW_STATE,' ParentID updated: ',NEW.parent_id);
    SET V_CHECK = 1;
    END IF;
    IF coalesce(NEW.hod,'NULL') <> coalesce(OLD.hod,'NULL') THEN
    SET V_PREV_STATE = concat(V_PREV_STATE,' HOD updated: ',coalesce(OLD.hod,'NULL'));
    SET V_NEW_STATE = concat(V_NEW_STATE,' HOD updated: ',coalesce(NEW.hod,'NULL'));
    SET V_CHECK = 1;
    END IF;
    IF coalesce(NEW.designee1,'NULL') <> coalesce(OLD.designee1,'NULL') THEN
    SET V_PREV_STATE = concat(V_PREV_STATE,' Designee-1 updated: ',coalesce(OLD.designee1,'NULL'));
    SET V_NEW_STATE = concat(V_NEW_STATE,' Designee-1 updated: ',coalesce(NEW.designee1,'NULL'));
    SET V_CHECK = 1;
    END IF;
    IF coalesce(NEW.designee2,'NULL') <> coalesce(OLD.designee2,'NULL') THEN
    SET V_PREV_STATE = concat(V_PREV_STATE,' Designee-2 updated: ',coalesce(OLD.designee2,'NULL'));
    SET V_NEW_STATE = concat(V_NEW_STATE,' Designee-2 updated: ',coalesce(NEW.designee2,'NULL'));
    SET V_CHECK = 1;
    END IF;
    IF coalesce(NEW.code,'NULL') <> coalesce(OLD.code,'NULL') THEN
    SET V_PREV_STATE = concat(V_PREV_STATE,' Code updated: ',coalesce(OLD.code,'NULL'));
    SET V_NEW_STATE = concat(V_NEW_STATE,' Code updated: ',coalesce(NEW.code,'NULL'));
    SET V_CHECK = 1;
    END IF;

    IF V_CHECK = 0 THEN
    SET V_NEW_STATE = 'Department Unchanged';
    END IF;

	insert into audit_trail (type,pk_value,action,prev_state,new_state,username) values (V_TYPE, V_PK_VALUE,V_ACTION,V_PREV_STATE, V_NEW_STATE, V_USERNAME);

END; ^;
