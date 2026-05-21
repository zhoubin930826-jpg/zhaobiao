module.exports = {
  root: true,
  env: {
    node: true
  },
  'extends': [
    'plugin:vue/essential',
    '@vue/standard'
  ],
  rules: {
    "semi": [0],
    'indent': 'off',
    // babel-eslint 10 + @babel/parser 7.29 的 token.type 非字符串，与 eslint-plugin-vue@4 不兼容
    'vue/script-indent': 'off',
    'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'vue/no-parsing-error': [
        2,
      {
        "x-invalid-end-tag": false
      }
    ]
  },
  parserOptions: {
    parser: 'babel-eslint'
  }
}
