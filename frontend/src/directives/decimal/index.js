export default {
    install: (Vue, options) => {
        // todo 需要增加循环遍历的方法进行install 这里暂时未分模块，待后期处理
        Vue.directive('decimal', {
            inserted: function (el) {
                el.addEventListener('input', function (e) {
                    // var that = this
                    // 通过正则过滤小数点后两位
                    let value = e.target.value
                    let newValue = limitDecimal(value)
                    e.target.value = newValue
                })
                let input = el.querySelector('input')
                input.addEventListener('blur', function (e) {
                    let value = e.target.value
                    e.target.value = parseFloat(value) ? parseFloat(value) + '' : ''
                })
            }
        })
    }
}

function limitDecimal (value) {
    if (validDecimal(value)) {
        return value
    } else {
        if (value.indexOf('.') !== value.length - 1) {
            if (value[0] === '0') {
                return parseInt(value)
            }
            return value.slice(0, value.length - 1)
        } else {
            return value === '.' ? '' : value
        }
    }
}
export function validDecimal (str) {
    const reg = /^(([1-9][0-9]*)|(([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))|(0))$/
    return reg.test(str)
}
