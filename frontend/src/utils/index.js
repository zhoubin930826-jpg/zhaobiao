export function setCheck (datas, ids) { // 遍历树设置节点是否选中
    for (var i in datas) {
        if (datas[i].child) {
            setCheck(datas[i].child, ids);
        } else {
            if (ids.includes(datas[i].id)) {
                datas[i].checked = true
                datas[i].selected = true
            } else {
                datas[i].checked = false
                datas[i].selected = false
            }
        }
    }
}

export function mapTree (org) {
    const haveChildren = Array.isArray(org.child) && org.child.length > 0;
    return {
        // 分别将我们查询出来的值做出改变他的key
        id: org.id,
        title: org.title,
        value: Number(org.id),
        label: org.title,
        // 判断它是否存在子集，若果存在就进行再次进行遍历操作，知道不存在子集便对其他的元素进行操作
        children: haveChildren ? org.child.map(i => mapTree(i)) : null
    };
}

export function downloadFile (src, filename) {
    let a = document.createElement('a')
    a.href = src
    a.download = filename
    a.click()
    a = null
}
