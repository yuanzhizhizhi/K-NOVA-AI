import { reactive } from 'vue';
const state = reactive({ visible: false, kind: 'alert', title: '', message: '', confirmText: '确定', cancelText: '取消', danger: false, inputType: 'text', placeholder: '', value: '' });
let resolver;
function open(kind, options) { return new Promise(resolve => { resolver = resolve; Object.assign(state, { visible: true, kind, title: options.title || { alert: '提示', confirm: '请确认', prompt: '请输入' }[kind], message: options.message, confirmText: options.confirmText || '确定', cancelText: options.cancelText || '取消', danger: !!options.danger, inputType: options.inputType || 'text', placeholder: options.placeholder || '', value: '' }); }); }
function finish(confirmed) { const result = confirmed ? (state.kind === 'prompt' ? state.value : true) : (state.kind === 'alert' ? true : null); state.visible = false; resolver?.(result); resolver = undefined; }
export const appDialog = { state, finish, alert: (options) => open('alert', options), confirm: (options) => open('confirm', options), prompt: (options) => open('prompt', options) };
