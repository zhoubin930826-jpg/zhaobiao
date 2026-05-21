module.exports={
  "root": true,
  "env": {
    "node": true
  },
  "extends": [
    "plugin:vue/essential",
    "@vue/standard"
  ],
  "rules": {
    "semi": [
      0
    ],
    "indent": "off",
    "vue/script-indent": "off",
    "no-console": "off",
    "no-debugger": "off",
    "vue/no-parsing-error": [
      2,
      {
        "x-invalid-end-tag": false
      }
    ]
  },
  "parserOptions": {
    "parser": "babel-eslint"
  }
}