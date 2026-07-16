import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { api } from '../api';
import { ArrowRight, CheckCircle2 } from 'lucide-vue-next';
const username = ref('admin'), password = ref('admin123'), loading = ref(false), error = ref(''), router = useRouter();
async function login() { loading.value = true; error.value = ''; try {
    const { data } = await api.post('/auth/login', { username: username.value, password: password.value });
    localStorage.setItem('token', data.token);
    localStorage.setItem('name', data.name);
    localStorage.setItem('role', data.role);
    if (data.avatarUrl)
        localStorage.setItem('avatarUrl', data.avatarUrl);
    else
        localStorage.removeItem('avatarUrl');
    router.push('/');
}
catch {
    error.value = '用户名或密码错误，或账号已被禁用';
}
finally {
    loading.value = false;
} }
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.main, __VLS_intrinsics.main)({
    ...{ class: "login" },
});
/** @type {__VLS_StyleScopedClasses['login']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
    ...{ class: "login-story" },
});
/** @type {__VLS_StyleScopedClasses['login-story']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "brand" },
});
/** @type {__VLS_StyleScopedClasses['brand']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "brand-mark ggbond-logo" },
});
/** @type {__VLS_StyleScopedClasses['brand-mark']} */ ;
/** @type {__VLS_StyleScopedClasses['ggbond-logo']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.img)({
    src: "/ggbond-logo.jpg",
    alt: "GGBOND logo",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "story-copy" },
});
/** @type {__VLS_StyleScopedClasses['story-copy']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "eyebrow" },
});
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h1, __VLS_intrinsics.h1)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.br)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.em, __VLS_intrinsics.em)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.ul, __VLS_intrinsics.ul)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({});
let __VLS_0;
/** @ts-ignore @type { | typeof __VLS_components.CheckCircle2} */
CheckCircle2;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({}));
const __VLS_2 = __VLS_1({}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({});
let __VLS_5;
/** @ts-ignore @type { | typeof __VLS_components.CheckCircle2} */
CheckCircle2;
// @ts-ignore
const __VLS_6 = __VLS_asFunctionalComponent1(__VLS_5, new __VLS_5({}));
const __VLS_7 = __VLS_6({}, ...__VLS_functionalComponentArgsRest(__VLS_6));
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({});
let __VLS_10;
/** @ts-ignore @type { | typeof __VLS_components.CheckCircle2} */
CheckCircle2;
// @ts-ignore
const __VLS_11 = __VLS_asFunctionalComponent1(__VLS_10, new __VLS_10({}));
const __VLS_12 = __VLS_11({}, ...__VLS_functionalComponentArgsRest(__VLS_11));
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "login-mascot" },
});
/** @type {__VLS_StyleScopedClasses['login-mascot']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.img)({
    src: "/ggbond-logo.jpg",
    alt: "GGBOND AI mascot",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "orbit orbit-a" },
});
/** @type {__VLS_StyleScopedClasses['orbit']} */ ;
/** @type {__VLS_StyleScopedClasses['orbit-a']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "orbit orbit-b" },
});
/** @type {__VLS_StyleScopedClasses['orbit']} */ ;
/** @type {__VLS_StyleScopedClasses['orbit-b']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
    ...{ class: "login-panel" },
});
/** @type {__VLS_StyleScopedClasses['login-panel']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.form, __VLS_intrinsics.form)({
    ...{ onSubmit: (__VLS_ctx.login) },
});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "mobile-brand" },
});
/** @type {__VLS_StyleScopedClasses['mobile-brand']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "eyebrow" },
});
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
    ...{ class: "muted" },
});
/** @type {__VLS_StyleScopedClasses['muted']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    autocomplete: "username",
    placeholder: "请输入账号",
});
(__VLS_ctx.username);
__VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    type: "password",
    autocomplete: "current-password",
    placeholder: "请输入密码",
});
(__VLS_ctx.password);
if (__VLS_ctx.error) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
        ...{ class: "error" },
    });
    /** @type {__VLS_StyleScopedClasses['error']} */ ;
    (__VLS_ctx.error);
}
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ class: "primary login-btn" },
    disabled: (__VLS_ctx.loading),
});
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
/** @type {__VLS_StyleScopedClasses['login-btn']} */ ;
(__VLS_ctx.loading ? '正在验证...' : '进入工作台');
let __VLS_15;
/** @ts-ignore @type { | typeof __VLS_components.ArrowRight} */
ArrowRight;
// @ts-ignore
const __VLS_16 = __VLS_asFunctionalComponent1(__VLS_15, new __VLS_15({
    size: (18),
}));
const __VLS_17 = __VLS_16({
    size: (18),
}, ...__VLS_functionalComponentArgsRest(__VLS_16));
__VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
    ...{ class: "hint" },
});
/** @type {__VLS_StyleScopedClasses['hint']} */ ;
// @ts-ignore
[login, username, password, error, error, loading, loading,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
