package com.zhaobiao.admin.config;

import com.zhaobiao.admin.common.SystemConstants;
import com.zhaobiao.admin.entity.BusinessType;
import com.zhaobiao.admin.entity.Menu;
import com.zhaobiao.admin.entity.MenuType;
import com.zhaobiao.admin.entity.Permission;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserStatus;
import com.zhaobiao.admin.repository.MenuRepository;
import com.zhaobiao.admin.repository.BusinessTypeRepository;
import com.zhaobiao.admin.repository.PermissionRepository;
import com.zhaobiao.admin.repository.RoleRepository;
import com.zhaobiao.admin.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    public DataInitializer(PermissionRepository permissionRepository,
                           MenuRepository menuRepository,
                           BusinessTypeRepository businessTypeRepository,
                           RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           BootstrapAdminProperties bootstrapAdminProperties) {
        this.permissionRepository = permissionRepository;
        this.menuRepository = menuRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapAdminProperties = bootstrapAdminProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Map<String, String> permissions = new LinkedHashMap<>();
        permissions.put("dashboard:view", "查看工作台");
        permissions.put("profile:view", "查看个人信息");
        permissions.put("profile:edit", "修改个人信息");
        permissions.put("admin:user:view", "查看管理员账号");
        permissions.put("admin:user:create", "新增管理员账号");
        permissions.put("admin:user:edit", "修改管理员账号");
        permissions.put("admin:user:status:update", "修改管理员状态");
        permissions.put("admin:user:password:reset", "重置管理员密码");
        permissions.put("admin:user:role:update", "修改管理员角色");
        permissions.put("member:view", "查看会员");
        permissions.put("member:create", "新增会员");
        permissions.put("member:edit", "修改会员");
        permissions.put("member:download:update", "修改会员下载权限");
        permissions.put("member:status:update", "修改会员状态");
        permissions.put("member:password:reset", "重置会员密码");
        permissions.put("business:type:view", "查看业务类型");
        permissions.put("business:type:create", "新增业务类型");
        permissions.put("business:type:edit", "修改业务类型");
        permissions.put("business:type:status:update", "修改业务类型状态");
        permissions.put("business:type:delete", "删除业务类型");
        permissions.put("tender:view", "查看招标");
        permissions.put("tender:create", "新增招标");
        permissions.put("tender:edit", "修改招标");
        permissions.put("tender:delete", "删除招标");
        permissions.put("tender:file:upload", "上传招标附件");
        permissions.put("user:view", "查看用户");
        permissions.put("user:audit", "审核用户");
        permissions.put("user:role:update", "修改用户角色");
        permissions.put("user:audit:record:view", "查看用户审核记录");
        permissions.put("role:view", "查看角色");
        permissions.put("role:edit", "维护角色");
        permissions.put("permission:view", "查看权限");
        permissions.put("permission:edit", "维护权限");
        permissions.put("menu:view", "查看菜单");
        permissions.put("menu:edit", "维护菜单");
        permissions.put("operation:log:view", "查看操作日志");

        permissions.forEach(this::upsertPermission);

        Menu dashboard = upsertMenu("DASHBOARD", "工作台", MenuType.MENU, null,
                "/dashboard/console", "dashboard/console", "House", 10, true, true, "dashboard:view", "系统首页");
        Menu profile = upsertMenu("PROFILE", "个人中心", MenuType.MENU, null,
                "/profile/index", "profile/index", "User", 20, true, true, "profile:view", "个人信息维护");
        Menu systemRoot = upsertMenu("SYSTEM_ROOT", "系统管理", MenuType.DIRECTORY, null,
                "/system", "", "Setting", 30, true, true, null, "系统管理目录");
        Menu adminUserManage = upsertMenu("SYSTEM_ADMIN_USER", "管理员管理", MenuType.MENU, systemRoot.getId(),
                "/system/user", "sys/user", "UserFilled", 10, true, true, "admin:user:view", "管理员账号管理页面");
        Menu memberUserManage = upsertMenu("SYSTEM_MEMBER_USER", "会员管理", MenuType.MENU, systemRoot.getId(),
                "/system/member", "sys/member", "User", 20, true, true, "member:view", "会员账号管理页面");
        Menu businessTypeManage = upsertMenu("SYSTEM_BUSINESS_TYPE", "类型管理", MenuType.MENU, systemRoot.getId(),
                "/system/business-type", "sys/business-type", "Collection", 30, true, true, "business:type:view", "业务类型管理页面");
        Menu tenderManage = upsertMenu("SYSTEM_TENDER", "招标管理", MenuType.MENU, systemRoot.getId(),
                "/tenders", "sys/tender", "Document", 40, true, true, "tender:view", "招标信息管理页面");
        Menu userManage = upsertMenu("SYSTEM_USER", "旧用户管理", MenuType.MENU, systemRoot.getId(),
                "/users", "users/index", "UserFilled", 50, false, false, "user:view", "历史用户管理页面（已停用）");
        Menu auditRecord = upsertMenu("SYSTEM_AUDIT_RECORD", "审核记录", MenuType.MENU, systemRoot.getId(),
                "/audit-records", "audit-records/index", "Document", 60, false, false, "user:audit:record:view", "历史用户审核记录页面（已停用）");
        Menu roleManage = upsertMenu("SYSTEM_ROLE", "角色管理", MenuType.MENU, systemRoot.getId(),
                "/system/role", "sys/role", "Avatar", 70, true, true, "role:view", "角色管理页面");
        Menu permissionManage = upsertMenu("SYSTEM_PERMISSION", "权限管理", MenuType.MENU, systemRoot.getId(),
                "/system/permissions", "sys/permissions", "Lock", 80, true, true, "permission:view", "权限管理页面");
        Menu menuManage = upsertMenu("SYSTEM_MENU", "菜单管理", MenuType.MENU, systemRoot.getId(),
                "/system/menu", "sys/menu", "Menu", 90, true, true, "menu:view", "菜单管理页面");
        Menu operationLog = upsertMenu("SYSTEM_OPERATION_LOG", "操作日志", MenuType.MENU, systemRoot.getId(),
                "/log", "system/log", "Tickets", 100, true, true, "operation:log:view", "操作日志页面");

        upsertMenu("MEMBER_CREATE_BUTTON", "新增会员按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 5, false, true, "member:create", "新增会员按钮");
        upsertMenu("MEMBER_EDIT_BUTTON", "编辑会员按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 8, false, true, "member:edit", "编辑会员按钮");
        upsertMenu("ADMIN_USER_CREATE_BUTTON", "新增管理员按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 10, false, true, "admin:user:create", "新增管理员按钮");
        upsertMenu("ADMIN_USER_EDIT_BUTTON", "编辑管理员按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 20, false, true, "admin:user:edit", "编辑管理员按钮");
        upsertMenu("ADMIN_USER_STATUS_BUTTON", "管理员状态按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 30, false, true, "admin:user:status:update", "修改管理员状态按钮");
        upsertMenu("ADMIN_USER_PASSWORD_BUTTON", "管理员重置密码按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 40, false, true, "admin:user:password:reset", "管理员重置密码按钮");
        upsertMenu("ADMIN_USER_ROLE_BUTTON", "管理员角色按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 50, false, true, "admin:user:role:update", "管理员角色按钮");
        upsertMenu("MEMBER_DOWNLOAD_BUTTON", "会员下载权限按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 10, false, true, "member:download:update", "会员下载权限按钮");
        upsertMenu("MEMBER_STATUS_BUTTON", "会员状态按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 20, false, true, "member:status:update", "会员状态按钮");
        upsertMenu("MEMBER_PASSWORD_BUTTON", "会员重置密码按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 30, false, true, "member:password:reset", "会员重置密码按钮");
        upsertMenu("BUSINESS_TYPE_CREATE_BUTTON", "新增类型按钮", MenuType.BUTTON, businessTypeManage.getId(),
                "", "", "", 10, false, true, "business:type:create", "新增类型按钮");
        upsertMenu("BUSINESS_TYPE_EDIT_BUTTON", "编辑类型按钮", MenuType.BUTTON, businessTypeManage.getId(),
                "", "", "", 20, false, true, "business:type:edit", "编辑类型按钮");
        upsertMenu("BUSINESS_TYPE_STATUS_BUTTON", "类型状态按钮", MenuType.BUTTON, businessTypeManage.getId(),
                "", "", "", 30, false, true, "business:type:status:update", "类型状态按钮");
        upsertMenu("BUSINESS_TYPE_DELETE_BUTTON", "删除类型按钮", MenuType.BUTTON, businessTypeManage.getId(),
                "", "", "", 40, false, true, "business:type:delete", "删除类型按钮");
        upsertMenu("TENDER_CREATE_BUTTON", "新增招标按钮", MenuType.BUTTON, tenderManage.getId(),
                "", "", "", 10, false, true, "tender:create", "新增招标按钮");
        upsertMenu("TENDER_EDIT_BUTTON", "编辑招标按钮", MenuType.BUTTON, tenderManage.getId(),
                "", "", "", 20, false, true, "tender:edit", "编辑招标按钮");
        upsertMenu("TENDER_DELETE_BUTTON", "删除招标按钮", MenuType.BUTTON, tenderManage.getId(),
                "", "", "", 30, false, true, "tender:delete", "删除招标按钮");
        upsertMenu("TENDER_UPLOAD_BUTTON", "上传附件按钮", MenuType.BUTTON, tenderManage.getId(),
                "", "", "", 40, false, true, "tender:file:upload", "上传招标附件按钮");
        upsertMenu("USER_AUDIT_BUTTON", "审核按钮", MenuType.BUTTON, userManage.getId(),
                "", "", "", 10, false, false, "user:audit", "旧用户审核按钮（已停用）");
        upsertMenu("USER_ROLE_BUTTON", "分配角色按钮", MenuType.BUTTON, userManage.getId(),
                "", "", "", 20, false, false, "user:role:update", "旧用户分配角色按钮（已停用）");
        upsertMenu("ROLE_EDIT_BUTTON", "角色维护按钮", MenuType.BUTTON, roleManage.getId(),
                "", "", "", 10, false, true, "role:edit", "角色维护按钮");
        upsertMenu("PERMISSION_EDIT_BUTTON", "权限维护按钮", MenuType.BUTTON, permissionManage.getId(),
                "", "", "", 10, false, true, "permission:edit", "权限维护按钮");
        upsertMenu("MENU_EDIT_BUTTON", "菜单维护按钮", MenuType.BUTTON, menuManage.getId(),
                "", "", "", 10, false, true, "menu:edit", "菜单维护按钮");
        upsertMenu("PROFILE_EDIT_BUTTON", "编辑个人信息按钮", MenuType.BUTTON, profile.getId(),
                "", "", "", 10, false, true, "profile:edit", "编辑个人信息按钮");

        Role superAdmin = upsertRole(
                SystemConstants.SUPER_ADMIN_ROLE,
                "超级管理员",
                "拥有系统全部权限",
                true,
                permissions.keySet().toArray(new String[0]),
                menuRepository.findAllByOrderBySortOrderAscIdAsc().stream().map(Menu::getCode).toArray(String[]::new)
        );

        upsertRole(
                SystemConstants.SYSTEM_ADMIN_ROLE,
                "系统管理员",
                "负责会员、招标、角色、权限、菜单和日志管理",
                true,
                new String[]{"dashboard:view", "profile:view", "profile:edit", "member:view", "member:create", "member:edit", "member:download:update", "member:status:update", "member:password:reset", "business:type:view", "business:type:create", "business:type:edit", "business:type:status:update", "business:type:delete", "tender:view", "tender:create", "tender:edit", "tender:delete", "tender:file:upload", "role:view", "role:edit", "permission:view", "permission:edit", "menu:view", "menu:edit", "operation:log:view"},
                new String[]{"DASHBOARD", "PROFILE", "SYSTEM_ROOT", "SYSTEM_MEMBER_USER", "SYSTEM_BUSINESS_TYPE", "SYSTEM_TENDER", "SYSTEM_ROLE", "SYSTEM_PERMISSION", "SYSTEM_MENU", "SYSTEM_OPERATION_LOG", "MEMBER_CREATE_BUTTON", "MEMBER_EDIT_BUTTON", "MEMBER_DOWNLOAD_BUTTON", "MEMBER_STATUS_BUTTON", "MEMBER_PASSWORD_BUTTON", "BUSINESS_TYPE_CREATE_BUTTON", "BUSINESS_TYPE_EDIT_BUTTON", "BUSINESS_TYPE_STATUS_BUTTON", "BUSINESS_TYPE_DELETE_BUTTON", "TENDER_CREATE_BUTTON", "TENDER_EDIT_BUTTON", "TENDER_DELETE_BUTTON", "TENDER_UPLOAD_BUTTON", "ROLE_EDIT_BUTTON", "PERMISSION_EDIT_BUTTON", "MENU_EDIT_BUTTON", "PROFILE_EDIT_BUTTON"}
        );

        upsertRole(
                SystemConstants.USER_AUDITOR_ROLE,
                "用户审核员",
                "历史用户审核角色，当前仅保留基础访问能力",
                true,
                new String[]{"dashboard:view", "profile:view", "profile:edit"},
                new String[]{"DASHBOARD", "PROFILE", "PROFILE_EDIT_BUTTON"}
        );

        upsertRole(
                SystemConstants.NORMAL_USER_ROLE,
                "普通用户",
                "默认注册用户角色，仅允许查看和维护个人信息",
                true,
                new String[]{"dashboard:view", "profile:view", "profile:edit"},
                new String[]{"DASHBOARD", "PROFILE", "PROFILE_EDIT_BUTTON"}
        );

        createInitialSuperAdminIfMissing(superAdmin);

        upsertBusinessType("ENGINEERING", "工程", 10, "工程类业务");
        upsertBusinessType("GOODS", "货物", 20, "货物类业务");
        upsertBusinessType("SERVICE", "服务", 30, "服务类业务");
    }

    private void createInitialSuperAdminIfMissing(Role superAdmin) {
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        String initialPassword = normalizeBootstrapPassword();
        if (!StringUtils.hasText(initialPassword)) {
            throw new IllegalStateException("检测到系统尚未初始化，请通过 APP_BOOTSTRAP_ADMIN_PASSWORD 配置初始超级管理员密码");
        }
        if (initialPassword.length() < 8) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD 长度不能少于 8 位");
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPhone("13900000000");
        admin.setEmail("admin@zhaobiao.com");
        admin.setPassword(passwordEncoder.encode(initialPassword));
        admin.setStatus(UserStatus.APPROVED);
        admin.setRealName("超级管理员");
        admin.setCompanyName("平台运营中心");
        admin.setContactPerson("超级管理员");
        admin.setUnifiedSocialCreditCode("91310000MA1KADMIN00");
        admin.setRoles(new LinkedHashSet<Role>() {{
            add(superAdmin);
        }});
        userRepository.save(admin);
    }

    private String normalizeBootstrapPassword() {
        if (!StringUtils.hasText(bootstrapAdminProperties.getAdminPassword())) {
            return null;
        }
        return bootstrapAdminProperties.getAdminPassword().trim();
    }

    private Permission upsertPermission(String code, String name) {
        Permission permission = permissionRepository.findByCode(code).orElseGet(Permission::new);
        permission.setCode(code);
        permission.setName(name);
        permission.setDescription(name);
        return permissionRepository.save(permission);
    }

    private Menu upsertMenu(String code,
                            String name,
                            MenuType type,
                            Long parentId,
                            String routePath,
                            String component,
                            String icon,
                            Integer sortOrder,
                            boolean visible,
                            boolean enabled,
                            String permissionCode,
                            String description) {
        Menu menu = menuRepository.findByCode(code).orElseGet(Menu::new);
        menu.setCode(code);
        menu.setName(name);
        menu.setType(type);
        menu.setParentId(parentId);
        menu.setRoutePath(routePath);
        menu.setComponent(component);
        menu.setIcon(icon);
        menu.setSortOrder(sortOrder);
        menu.setVisible(visible);
        menu.setEnabled(enabled);
        menu.setPermissionCode(permissionCode);
        menu.setDescription(description);
        return menuRepository.save(menu);
    }

    private Role upsertRole(String code,
                            String name,
                            String description,
                            boolean builtIn,
                            String[] permissionCodes,
                            String[] menuCodes) {
        Role role = roleRepository.findByCode(code).orElseGet(Role::new);
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setBuiltIn(builtIn);
        role.setPermissions(Arrays.stream(permissionCodes)
                .map(item -> permissionRepository.findByCode(item).orElseThrow(() -> new IllegalStateException("权限不存在: " + item)))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        role.setMenus(Arrays.stream(menuCodes)
                .map(item -> menuRepository.findByCode(item).orElseThrow(() -> new IllegalStateException("菜单不存在: " + item)))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return roleRepository.save(role);
    }

    private void upsertBusinessType(String code, String name, int sortOrder, String description) {
        BusinessType businessType = businessTypeRepository.findByCode(code).orElseGet(BusinessType::new);
        businessType.setCode(code);
        businessType.setName(name);
        businessType.setEnabled(true);
        businessType.setSortOrder(sortOrder);
        businessType.setDescription(description);
        businessTypeRepository.save(businessType);
    }
}
