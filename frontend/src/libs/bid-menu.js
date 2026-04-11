/**
 * 招标后台 OpenAPI：菜单 DTO 与前端侧栏 AuthTree / 系统菜单树展示结构的转换
 */

export function menuDtosToAuthTree (menus) {
    if (!menus || !menus.length) return [];
    return menus.map(m => ({
        id: String(m.id),
        title: m.name,
        fe_path: m.routePath || '/',
        icon: m.icon || '',
        type: m.type,
        visible: m.visible,
        enabled: m.enabled,
        child: menuDtosToAuthTree(m.children || [])
    }));
}

/** 系统-菜单管理页：后端菜单树 → iView Tree（children-key 为 child） */
export function menuDtosToSysTree (menus, level = 1) {
    return (menus || []).map(m => ({
        id: m.id,
        title: m.name,
        name: m.name,
        code: m.code,
        type: m.type,
        parentId: m.parentId,
        routePath: m.routePath,
        fe_path: m.routePath || '',
        component: m.component,
        icon: m.icon || '',
        sortOrder: m.sortOrder,
        sort: String(m.sortOrder != null ? m.sortOrder : 0),
        visible: m.visible,
        enabled: m.enabled,
        description: m.description,
        remark: m.description || '',
        pid: m.parentId != null ? m.parentId : null,
        level,
        status: m.enabled ? '1' : '0',
        child: menuDtosToSysTree(m.children || [], level + 1)
    }));
}
