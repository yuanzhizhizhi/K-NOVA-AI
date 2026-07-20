import { nextTick, ref, watch } from 'vue';
import { AlertTriangle, CheckCircle2, HelpCircle, X } from 'lucide-vue-next';
import { appDialog } from './appDialog';
const input = ref();
watch(() => appDialog.state.visible, value => { if (value && appDialog.state.kind === 'prompt')
    nextTick(() => input.value?.focus()); });
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['dialog-x']} */ ;
/** @type {__VLS_StyleScopedClasses['dialog-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['dialog-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
/** @type {__VLS_StyleScopedClasses['dialog-confirm']} */ ;
/** @type {__VLS_StyleScopedClasses['danger']} */ ;
/** @type {__VLS_StyleScopedClasses['dialog-confirm']} */ ;
/** @type {__VLS_StyleScopedClasses['dialog-fade-enter-from']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
/** @type {__VLS_StyleScopedClasses['dialog-fade-leave-to']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
/** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
let __VLS_0;
/** @ts-ignore @type { | typeof __VLS_components.Teleport | typeof __VLS_components.Teleport} */
Teleport;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
    to: "body",
}));
const __VLS_2 = __VLS_1({
    to: "body",
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
const { default: __VLS_5 } = __VLS_3.slots;
let __VLS_6;
/** @ts-ignore @type { | typeof __VLS_components.Transition | typeof __VLS_components.Transition} */
Transition;
// @ts-ignore
const __VLS_7 = __VLS_asFunctionalComponent1(__VLS_6, new __VLS_6({
    name: "dialog-fade",
}));
const __VLS_8 = __VLS_7({
    name: "dialog-fade",
}, ...__VLS_functionalComponentArgsRest(__VLS_7));
const { default: __VLS_11 } = __VLS_9.slots;
if (__VLS_ctx.appDialog.state.visible) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.appDialog.state.visible))
                    throw 0;
                return (__VLS_ctx.appDialog.finish(false));
                // @ts-ignore
                [appDialog, appDialog,];
            } },
        ...{ class: "app-dialog-backdrop" },
    });
    /** @type {__VLS_StyleScopedClasses['app-dialog-backdrop']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
        ...{ class: "app-dialog" },
        role: "dialog",
        'aria-modal': "true",
        'aria-label': (__VLS_ctx.appDialog.state.title),
    });
    /** @type {__VLS_StyleScopedClasses['app-dialog']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.appDialog.state.visible))
                    throw 0;
                return (__VLS_ctx.appDialog.finish(false));
                // @ts-ignore
                [appDialog, appDialog,];
            } },
        ...{ class: "dialog-x" },
    });
    /** @type {__VLS_StyleScopedClasses['dialog-x']} */ ;
    let __VLS_12;
    /** @ts-ignore @type { | typeof __VLS_components.X} */
    X;
    // @ts-ignore
    const __VLS_13 = __VLS_asFunctionalComponent1(__VLS_12, new __VLS_12({}));
    const __VLS_14 = __VLS_13({}, ...__VLS_functionalComponentArgsRest(__VLS_13));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "dialog-icon" },
        ...{ class: ({ danger: __VLS_ctx.appDialog.state.danger }) },
    });
    /** @type {__VLS_StyleScopedClasses['dialog-icon']} */ ;
    /** @type {__VLS_StyleScopedClasses['danger']} */ ;
    if (__VLS_ctx.appDialog.state.danger) {
        let __VLS_17;
        /** @ts-ignore @type { | typeof __VLS_components.AlertTriangle} */
        AlertTriangle;
        // @ts-ignore
        const __VLS_18 = __VLS_asFunctionalComponent1(__VLS_17, new __VLS_17({}));
        const __VLS_19 = __VLS_18({}, ...__VLS_functionalComponentArgsRest(__VLS_18));
    }
    else if (__VLS_ctx.appDialog.state.kind !== 'alert') {
        let __VLS_22;
        /** @ts-ignore @type { | typeof __VLS_components.HelpCircle} */
        HelpCircle;
        // @ts-ignore
        const __VLS_23 = __VLS_asFunctionalComponent1(__VLS_22, new __VLS_22({}));
        const __VLS_24 = __VLS_23({}, ...__VLS_functionalComponentArgsRest(__VLS_23));
    }
    else {
        let __VLS_27;
        /** @ts-ignore @type { | typeof __VLS_components.CheckCircle2} */
        CheckCircle2;
        // @ts-ignore
        const __VLS_28 = __VLS_asFunctionalComponent1(__VLS_27, new __VLS_27({}));
        const __VLS_29 = __VLS_28({}, ...__VLS_functionalComponentArgsRest(__VLS_28));
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.h3, __VLS_intrinsics.h3)({});
    (__VLS_ctx.appDialog.state.title);
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.appDialog.state.message);
    if (__VLS_ctx.appDialog.state.kind === 'prompt') {
        __VLS_asFunctionalElement1(__VLS_intrinsics.input, __VLS_intrinsics.input)({
            ...{ onKeydown: (...[$event]) => {
                    if (!(__VLS_ctx.appDialog.state.visible))
                        throw 0;
                    if (!(__VLS_ctx.appDialog.state.kind === 'prompt'))
                        throw 0;
                    return (__VLS_ctx.appDialog.state.value && __VLS_ctx.appDialog.finish(true));
                    // @ts-ignore
                    [appDialog, appDialog, appDialog, appDialog, appDialog, appDialog, appDialog, appDialog,];
                } },
            ref: "input",
            type: (__VLS_ctx.appDialog.state.inputType),
            placeholder: (__VLS_ctx.appDialog.state.placeholder),
        });
        (__VLS_ctx.appDialog.state.value);
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.footer, __VLS_intrinsics.footer)({});
    if (__VLS_ctx.appDialog.state.kind !== 'alert') {
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.appDialog.state.visible))
                        throw 0;
                    if (!(__VLS_ctx.appDialog.state.kind !== 'alert'))
                        throw 0;
                    return (__VLS_ctx.appDialog.finish(false));
                    // @ts-ignore
                    [appDialog, appDialog, appDialog, appDialog, appDialog,];
                } },
            ...{ class: "dialog-cancel" },
        });
        /** @type {__VLS_StyleScopedClasses['dialog-cancel']} */ ;
        (__VLS_ctx.appDialog.state.cancelText);
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.appDialog.state.visible))
                    throw 0;
                return (__VLS_ctx.appDialog.finish(true));
                // @ts-ignore
                [appDialog, appDialog,];
            } },
        ...{ class: "dialog-confirm" },
        ...{ class: ({ danger: __VLS_ctx.appDialog.state.danger }) },
        disabled: (__VLS_ctx.appDialog.state.kind === 'prompt' && !__VLS_ctx.appDialog.state.value),
    });
    /** @type {__VLS_StyleScopedClasses['dialog-confirm']} */ ;
    /** @type {__VLS_StyleScopedClasses['danger']} */ ;
    (__VLS_ctx.appDialog.state.confirmText);
}
// @ts-ignore
[appDialog, appDialog, appDialog, appDialog,];
var __VLS_9;
// @ts-ignore
[];
var __VLS_3;
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
