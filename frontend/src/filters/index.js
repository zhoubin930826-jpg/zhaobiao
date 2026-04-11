/**
 * @desc vue插件的写法 通过vue.use(filter) 来实现过滤器的注册
 */
import filters from './filters'
export default {
    install: (Vue, options) => {
        Object.keys(filters).forEach(key => {
            Vue.filter(key, filters[key])
        })
    }
}
