package com.zhaobiao.admin.config;

import com.zhaobiao.admin.common.SystemConstants;
import com.zhaobiao.admin.entity.BusinessType;
import com.zhaobiao.admin.entity.Menu;
import com.zhaobiao.admin.entity.MenuType;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserStatus;
import com.zhaobiao.admin.repository.MenuRepository;
import com.zhaobiao.admin.repository.BusinessTypeRepository;
import com.zhaobiao.admin.repository.RoleRepository;
import com.zhaobiao.admin.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class DataInitializer implements ApplicationRunner {

    private final MenuRepository menuRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties bootstrapAdminProperties;

    public DataInitializer(MenuRepository menuRepository,
                           BusinessTypeRepository businessTypeRepository,
                           RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           BootstrapAdminProperties bootstrapAdminProperties) {
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
        Menu dashboard = upsertMenu("DASHBOARD", "工作台", MenuType.MENU, null,
                "/dashboard/console", "dashboard/console", "House", 10, true, true, "系统首页");
        Menu profile = upsertMenu("PROFILE", "个人中心", MenuType.MENU, null,
                "/profile/index", "profile/index", "User", 20, true, true, "个人信息维护");
        Menu systemRoot = upsertMenu("SYSTEM_ROOT", "系统管理", MenuType.DIRECTORY, null,
                "/system", "", "Setting", 30, true, true, "系统管理目录");
        Menu adminUserManage = upsertMenu("SYSTEM_ADMIN_USER", "管理员管理", MenuType.MENU, systemRoot.getId(),
                "/system/user", "sys/user", "UserFilled", 10, true, true, "管理员账号管理页面");
        Menu memberUserManage = upsertMenu("SYSTEM_MEMBER_USER", "会员管理", MenuType.MENU, systemRoot.getId(),
                "/system/member", "sys/member", "User", 20, true, true, "会员账号管理页面");
        Menu businessTypeManage = upsertMenu("SYSTEM_BUSINESS_TYPE", "类型管理", MenuType.MENU, systemRoot.getId(),
                "/system/business-type", "sys/business-type", "Collection", 30, true, true, "业务类型管理页面");
        Menu tenderManage = upsertMenu("SYSTEM_TENDER", "招标管理", MenuType.MENU, systemRoot.getId(),
                "/tenders", "sys/tender", "Document", 40, true, true, "招标信息管理页面");
        Menu userManage = upsertMenu("SYSTEM_USER", "旧用户管理", MenuType.MENU, systemRoot.getId(),
                "/users", "users/index", "UserFilled", 50, false, false, "历史用户管理页面（已停用）");
        Menu auditRecord = upsertMenu("SYSTEM_AUDIT_RECORD", "审核记录", MenuType.MENU, systemRoot.getId(),
                "/audit-records", "audit-records/index", "Document", 60, false, false, "历史用户审核记录页面（已停用）");
        Menu roleManage = upsertMenu("SYSTEM_ROLE", "角色管理", MenuType.MENU, systemRoot.getId(),
                "/system/role", "sys/role", "Avatar", 70, true, true, "角色管理页面");
        Menu permissionManage = upsertMenu("SYSTEM_PERMISSION", "权限管理", MenuType.MENU, systemRoot.getId(),
                "/system/permissions", "sys/permissions", "Lock", 80, false, false, "权限管理已停用，授权以菜单/按钮编码为准");
        Menu menuManage = upsertMenu("SYSTEM_MENU", "菜单管理", MenuType.MENU, systemRoot.getId(),
                "/system/menu", "sys/menu", "Menu", 90, true, true, "菜单管理页面");
        Menu operationLog = upsertMenu("SYSTEM_OPERATION_LOG", "操作日志", MenuType.MENU, systemRoot.getId(),
                "/log", "system/log", "Tickets", 100, true, true, "操作日志页面");

        upsertMenu("MEMBER_CREATE_BUTTON", "新增会员按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 5, false, true, "新增会员按钮");
        upsertMenu("MEMBER_EDIT_BUTTON", "编辑会员按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 8, false, true, "编辑会员按钮");
        upsertMenu("ADMIN_USER_CREATE_BUTTON", "新增管理员按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 10, false, true, "新增管理员按钮");
        upsertMenu("ADMIN_USER_EDIT_BUTTON", "编辑管理员按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 20, false, true, "编辑管理员按钮");
        upsertMenu("ADMIN_USER_STATUS_BUTTON", "管理员状态按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 30, false, true, "修改管理员状态按钮");
        upsertMenu("ADMIN_USER_PASSWORD_BUTTON", "管理员重置密码按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 40, false, true, "管理员重置密码按钮");
        upsertMenu("ADMIN_USER_ROLE_BUTTON", "管理员角色按钮", MenuType.BUTTON, adminUserManage.getId(),
                "", "", "", 50, false, true, "管理员角色按钮");
        upsertMenu("MEMBER_DOWNLOAD_BUTTON", "会员下载权限按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 10, false, true, "会员下载权限按钮");
        upsertMenu("MEMBER_STATUS_BUTTON", "会员状态按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 20, false, true, "会员状态按钮");
        upsertMenu("MEMBER_PASSWORD_BUTTON", "会员重置密码按钮", MenuType.BUTTON, memberUserManage.getId(),
                "", "", "", 30, false, true, "会员重置密码按钮");
        upsertMenu("BUSINESS_TYPE_CREATE_BUTTON", "新增类型按钮", MenuType.BUTTON, businessTypeManage.getId(),
                "", "", "", 10, false, true, "新增类型按钮");
        upsertMenu("BUSINESS_TYPE_EDIT_BUTTON", "编辑类型按钮", MenuType.BUTTON, businessTypeManage.getId(),
                "", "", "", 20, false, true, "编辑类型按钮");
        upsertMenu("BUSINESS_TYPE_STATUS_BUTTON", "类型状态按钮", MenuType.BUTTON, businessTypeManage.getId(),
                "", "", "", 30, false, true, "类型状态按钮");
        upsertMenu("BUSINESS_TYPE_DELETE_BUTTON", "删除类型按钮", MenuType.BUTTON, businessTypeManage.getId(),
                "", "", "", 40, false, true, "删除类型按钮");
        upsertMenu("TENDER_CREATE_BUTTON", "新增招标按钮", MenuType.BUTTON, tenderManage.getId(),
                "", "", "", 10, false, true, "新增招标按钮");
        upsertMenu("TENDER_EDIT_BUTTON", "编辑招标按钮", MenuType.BUTTON, tenderManage.getId(),
                "", "", "", 20, false, true, "编辑招标按钮");
        upsertMenu("TENDER_DELETE_BUTTON", "删除招标按钮", MenuType.BUTTON, tenderManage.getId(),
                "", "", "", 30, false, true, "删除招标按钮");
        upsertMenu("TENDER_UPLOAD_BUTTON", "上传附件按钮", MenuType.BUTTON, tenderManage.getId(),
                "", "", "", 40, false, true, "上传招标附件按钮");
        upsertMenu("USER_AUDIT_BUTTON", "审核按钮", MenuType.BUTTON, userManage.getId(),
                "", "", "", 10, false, false, "旧用户审核按钮（已停用）");
        upsertMenu("USER_ROLE_BUTTON", "分配角色按钮", MenuType.BUTTON, userManage.getId(),
                "", "", "", 20, false, false, "旧用户分配角色按钮（已停用）");
        upsertMenu("ROLE_EDIT_BUTTON", "角色维护按钮", MenuType.BUTTON, roleManage.getId(),
                "", "", "", 10, false, true, "角色维护按钮");
        upsertMenu("PERMISSION_EDIT_BUTTON", "权限维护按钮", MenuType.BUTTON, permissionManage.getId(),
                "", "", "", 10, false, false, "权限管理已停用，授权以菜单/按钮编码为准");
        upsertMenu("MENU_EDIT_BUTTON", "菜单维护按钮", MenuType.BUTTON, menuManage.getId(),
                "", "", "", 10, false, true, "菜单维护按钮");
        upsertMenu("PROFILE_EDIT_BUTTON", "编辑个人信息按钮", MenuType.BUTTON, profile.getId(),
                "", "", "", 10, false, true, "编辑个人信息按钮");

        Role superAdmin = upsertRole(
                SystemConstants.SUPER_ADMIN_ROLE,
                "超级管理员",
                "拥有系统全部菜单和按钮",
                true,
                menuRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                        .filter(Menu::isEnabled)
                        .map(Menu::getCode)
                        .toArray(String[]::new)
        );

        upsertRole(
                SystemConstants.SYSTEM_ADMIN_ROLE,
                "系统管理员",
                "负责会员、招标、角色、菜单和日志管理",
                true,
                new String[]{"DASHBOARD", "PROFILE", "SYSTEM_ROOT", "SYSTEM_MEMBER_USER", "SYSTEM_BUSINESS_TYPE", "SYSTEM_TENDER", "SYSTEM_ROLE", "SYSTEM_MENU", "SYSTEM_OPERATION_LOG", "MEMBER_CREATE_BUTTON", "MEMBER_EDIT_BUTTON", "MEMBER_DOWNLOAD_BUTTON", "MEMBER_STATUS_BUTTON", "MEMBER_PASSWORD_BUTTON", "BUSINESS_TYPE_CREATE_BUTTON", "BUSINESS_TYPE_EDIT_BUTTON", "BUSINESS_TYPE_STATUS_BUTTON", "BUSINESS_TYPE_DELETE_BUTTON", "TENDER_CREATE_BUTTON", "TENDER_EDIT_BUTTON", "TENDER_DELETE_BUTTON", "TENDER_UPLOAD_BUTTON", "ROLE_EDIT_BUTTON", "MENU_EDIT_BUTTON", "PROFILE_EDIT_BUTTON"}
        );

        upsertRole(
                SystemConstants.USER_AUDITOR_ROLE,
                "用户审核员",
                "历史用户审核角色，当前仅保留基础访问能力",
                true,
                new String[]{"DASHBOARD", "PROFILE", "PROFILE_EDIT_BUTTON"}
        );

        upsertRole(
                SystemConstants.NORMAL_USER_ROLE,
                "普通用户",
                "默认注册用户角色，仅允许查看和维护个人信息",
                true,
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
        menu.setPermissionCode(null);
        menu.setDescription(description);
        return menuRepository.save(menu);
    }

    private Role upsertRole(String code,
                            String name,
                            String description,
                            boolean builtIn,
                            String[] menuCodes) {
        Role role = roleRepository.findByCode(code).orElseGet(Role::new);
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        role.setBuiltIn(builtIn);
        role.setPermissions(new LinkedHashSet<>());
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
