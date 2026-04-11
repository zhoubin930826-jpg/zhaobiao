export default {
    install: (Vue, options) => {
        // todo 需要增加循环遍历的方法进行install 这里暂时未分模块，待后期处理
        Vue.directive('int', {
            inserted: function (el) {
                el.addEventListener('input', function (e) {
                    let value = e.target.value
                    let newValue = limitDecimal(value)
                    console.log('int', value, newValue)
                    e.target.value = newValue
                })
                let input = el.querySelector('input')
                input.addEventListener('blur', function (e) {
                    let value = e.target.value
                    e.target.value = parseInt(value) ? parseInt(value) + '' : ''
                })
            }
        })
    }
}

function limitDecimal (value) {
    console.log(validInt(value))
    if (validInt(value)) {
        return value
    } else {
        if (value[0] === '0') {
            return parseInt(value)
        }
        return value.slice(0, value.length - 1)
    }
}
export function validInt (str) {
    const reg = /^(([1-9][0-9]*)|(0))$/
    return reg.test(str)
}
