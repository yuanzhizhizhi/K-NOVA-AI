import { ref, nextTick } from 'vue';
import { ArrowUp, Paperclip, Sparkles } from 'lucide-vue-next';
const __VLS_props = defineProps();
const emit = defineEmits();
const text = ref(''), input = ref();
function send() { const v = text.value.trim(); if (!v)
    return; emit('send', v); text.value = ''; nextTick(() => { if (input.value)
    input.value.style.height = '24px'; }); }
function resize(e) { const el = e.target; el.style.height = '24px'; el.style.height = Math.min(el.scrollHeight, 160) + 'px'; }
function key(e) { if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    send();
} } // @ts-ignore
const __VLS_ctx = {
    ...{},
    ...{},
    ...{},
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "composer" },
    ...{ class: ({ disabled: __VLS_ctx.disabled }) },
});
/** @type {__VLS_StyleScopedClasses['composer']} */ ;
/** @type {__VLS_StyleScopedClasses['disabled']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.textarea, __VLS_intrinsics.textarea)({
    ...{ onInput: (__VLS_ctx.resize) },
    ...{ onKeydown: (__VLS_ctx.key) },
    ref: "input",
    value: (__VLS_ctx.text),
    disabled: (__VLS_ctx.disabled),
    rows: "1",
    placeholder: "向知识库提问，Shift + Enter 换行…",
    'aria-label': "消息输入框",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "composer-actions" },
});
/** @type {__VLS_StyleScopedClasses['composer-actions']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ class: "icon-btn" },
    title: "附件将在下一版本支持",
});
/** @type {__VLS_StyleScopedClasses['icon-btn']} */ ;
let __VLS_0;
/** @ts-ignore @type { | typeof __VLS_components.Paperclip} */
Paperclip;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({}));
const __VLS_2 = __VLS_1({}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
let __VLS_5;
/** @ts-ignore @type { | typeof __VLS_components.Sparkles} */
Sparkles;
// @ts-ignore
const __VLS_6 = __VLS_asFunctionalComponent1(__VLS_5, new __VLS_5({}));
const __VLS_7 = __VLS_6({}, ...__VLS_functionalComponentArgsRest(__VLS_6));
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.send) },
    ...{ class: "send" },
    disabled: (!__VLS_ctx.text.trim() || __VLS_ctx.disabled),
    'aria-label': "发送",
});
/** @type {__VLS_StyleScopedClasses['send']} */ ;
let __VLS_10;
/** @ts-ignore @type { | typeof __VLS_components.ArrowUp} */
ArrowUp;
// @ts-ignore
const __VLS_11 = __VLS_asFunctionalComponent1(__VLS_10, new __VLS_10({}));
const __VLS_12 = __VLS_11({}, ...__VLS_functionalComponentArgsRest(__VLS_11));
// @ts-ignore
[disabled, disabled, disabled, resize, key, text, text, send,];
const __VLS_export = (await import('vue')).defineComponent({
    __typeEmits: {},
    __typeProps: {},
});
export default {};
