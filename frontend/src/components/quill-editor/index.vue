<template>
  <div class="i-quill-editor" :class="{ 'i-quill-editor--bordered': border }">
    <div ref="host" class="i-quill-editor__host"></div>
  </div>
</template>

<script>
    import Quill from 'quill';
    import 'quill/dist/quill.snow.css';

    export default {
        name: 'i-quill-editor',
        props: {
            value: {
                type: String,
                default: ''
            },
            border: {
                type: Boolean,
                default: false
            },
            placeholder: {
                type: String,
                default: '请输入正文'
            },
            minHeight: {
                type: Number,
                default: 260
            }
        },
        data () {
            return {
                quill: null,
                syncing: false
            };
        },
        computed: {
            toolbarOptions () {
                return [
                    [{ header: [1, 2, 3, false] }],
                    ['bold', 'italic', 'underline', 'strike'],
                    [{ color: [] }, { background: [] }],
                    [{ list: 'ordered' }, { list: 'bullet' }],
                    [{ indent: '-1' }, { indent: '+1' }],
                    [{ align: [] }],
                    ['blockquote', 'link'],
                    ['clean']
                ];
            }
        },
        watch: {
            value (val) {
                if (!this.quill || this.syncing) return;
                const next = val || '';
                const currentHtml = this.quill.root.innerHTML;
                if (this.normalizeHtml(currentHtml) === this.normalizeHtml(next)) return;
                this.syncing = true;
                const sel = this.quill.getSelection();
                this.quill.setContents([]);
                if (next) {
                    this.quill.clipboard.dangerouslyPasteHTML(next);
                }
                this.$nextTick(() => {
                    this.syncing = false;
                    if (sel) {
                        this.quill.setSelection(sel.index, sel.length, 'silent');
                    }
                });
            }
        },
        mounted () {
            this.quill = new Quill(this.$refs.host, {
                theme: 'snow',
                placeholder: this.placeholder,
                modules: {
                    toolbar: this.toolbarOptions
                }
            });
            const initial = this.value && String(this.value).trim() ? this.value : '';
            if (initial) {
                this.quill.clipboard.dangerouslyPasteHTML(initial);
            }
            const editorEl = this.$refs.host.querySelector('.ql-editor');
            if (editorEl) {
                editorEl.style.minHeight = `${this.minHeight}px`;
            }
            this.quill.on('text-change', () => {
                if (this.syncing) return;
                const html = this.quill.root.innerHTML;
                const normalized = this.normalizeHtml(html);
                this.$emit('input', normalized);
                this.$emit('on-change', normalized);
            });
        },
        beforeDestroy () {
            this.quill = null;
        },
        methods: {
            normalizeHtml (html) {
                if (!html) return '';
                const compact = String(html).replace(/\s+/g, '');
                if (compact === '<p><br></p>' || compact === '<p></p>' || compact === '<br>') {
                    return '';
                }
                return html;
            }
        }
    };
</script>

<style lang="less" scoped>
.i-quill-editor__host {
  min-height: 260px;
}

.i-quill-editor ::v-deep .ql-toolbar.ql-snow,
.i-quill-editor ::v-deep .ql-container.ql-snow {
  font-family: inherit;
}

.i-quill-editor ::v-deep .ql-container.ql-snow {
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
}

.i-quill-editor ::v-deep .ql-toolbar.ql-snow {
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}

.i-quill-editor ::v-deep .ql-snow .ql-picker.ql-expanded .ql-picker-options {
  z-index: 3000;
}

.i-quill-editor ::v-deep .ql-snow .ql-tooltip {
  z-index: 3000;
}

.i-quill-editor--bordered ::v-deep .ql-toolbar.ql-snow {
  border: 1px solid #dcdee2;
  border-bottom: none;
  background: #f8f8f9;
}

.i-quill-editor--bordered ::v-deep .ql-container.ql-snow {
  border: 1px solid #dcdee2;
}
</style>
