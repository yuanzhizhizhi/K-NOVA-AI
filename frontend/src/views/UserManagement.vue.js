import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, Check, KeyRound, Plus, Search, Trash2, UserCog, X } from 'lucide-vue-next';
import { api } from '../api';
import { appDialog } from '../components/appDialog';
const router = useRouter(), users = ref([]), query = ref(''), showCreate = ref(false), busy = ref(false);
const form = ref({ username: '', displayName: '', password: '', role: 'USER' });
const filtered = computed(() => users.value.filter(u => (u.username + ' ' + u.displayName).toLowerCase().includes(query.value.toLowerCase())));
async function load() { users.value = (await api.get('/users')).data; }
async function create() { busy.value = true; try {
    await api.post('/users', form.value);
    form.value = { username: '', displayName: '', password: '', role: 'USER' };
    showCreate.value = false;
    await load();
}
finally {
    busy.value = false;
} }
async function save(user) { await api.patch(`/users/${user.id}`, { displayName: user.displayName, role: user.role, enabled: user.enabled }); await load(); }
async function toggle(user) { user.enabled = !user.enabled; try {
    await save(user);
}
catch {
    user.enabled = !user.enabled;
} }
async function resetPassword(user) { const password = await appDialog.prompt({ title: '重置用户密码', message: `为 ${user.username} 设置新密码，密码至少需要 6 位。`, confirmText: '重置密码', inputType: 'password', placeholder: '请输入新密码' }); if (!password)
    return; if (password.length < 6) {
    await appDialog.alert({ title: '密码不符合要求', message: '新密码至少需要 6 位。' });
    return;
} await api.put(`/users/${user.id}/password`, { password }); await appDialog.alert({ title: '重置成功', message: `用户 ${user.username} 的密码已经更新。` }); }
async function remove(user) { if (!await appDialog.confirm({ title: '删除用户', message: `确认删除用户“${user.username}”吗？其普通空间成员关系也会被清理。`, confirmText: '删除用户', danger: true }))
    return; await api.delete(`/users/${user.id}`); await load(); }
onMounted(load);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['back']} */ ;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['search-box']} */ ;
/** @type {__VLS_StyleScopedClasses['search-box']} */ ;
/** @type {__VLS_StyleScopedClasses['table-head']} */ ;
/** @type {__VLS_StyleScopedClasses['user-table']} */ ;
/** @type {__VLS_StyleScopedClasses['user-name']} */ ;
/** @type {__VLS_StyleScopedClasses['user-name']} */ ;
/** @type {__VLS_StyleScopedClasses['user-name']} */ ;
/** @type {__VLS_StyleScopedClasses['user-name']} */ ;
/** @type {__VLS_StyleScopedClasses['user-table']} */ ;
/** @type {__VLS_StyleScopedClasses['status-switch']} */ ;
/** @type {__VLS_StyleScopedClasses['status-switch']} */ ;
/** @type {__VLS_StyleScopedClasses['status-switch']} */ ;
/** @type {__VLS_StyleScopedClasses['enabled']} */ ;
/** @type {__VLS_StyleScopedClasses['row-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['row-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['row-actions']} */ ;
/** @type {__VLS_StyleScopedClasses['no-users']} */ ;
/** @type {__VLS_StyleScopedClasses['modal']} */ ;
/** @type {__VLS_StyleScopedClasses['modal']} */ ;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
/** @type {__VLS_StyleScopedClasses['table-head']} */ ;
/** @type {__VLS_StyleScopedClasses['user-table']} */ ;
/** @type {__VLS_StyleScopedClasses['users-toolbar']} */ ;
/** @type {__VLS_StyleScopedClasses['search-box']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.main, __VLS_intrinsics.main)({
    ...{ class: "users-page" },
});
/** @type {__VLS_StyleScopedClasses['users-page']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.header, __VLS_intrinsics.header)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            return (__VLS_ctx.router.push('/'));
            // @ts-ignore
            [router,];
        } },
    ...{ class: "back" },
});
/** @type {__VLS_StyleScopedClasses['back']} */ ;
let __VLS_0;
/** @ts-ignore @type { | typeof __VLS_components.ArrowLeft} */
ArrowLeft;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({}));
const __VLS_2 = __VLS_1({}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "eyebrow" },
});
/** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h1, __VLS_intrinsics.h1)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            return (__VLS_ctx.showCreate = true);
            // @ts-ignore
            [showCreate,];
        } },
    ...{ class: "primary" },
});
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
let __VLS_5;
/** @ts-ignore @type { | typeof __VLS_components.Plus} */
Plus;
// @ts-ignore
const __VLS_6 = __VLS_asFunctionalComponent1(__VLS_5, new __VLS_5({}));
const __VLS_7 = __VLS_6({}, ...__VLS_functionalComponentArgsRest(__VLS_6));
__VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
    ...{ class: "users-card" },
});
/** @type {__VLS_StyleScopedClasses['users-card']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "users-toolbar" },
});
/** @type {__VLS_StyleScopedClasses['users-toolbar']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "search-box" },
});
/** @type {__VLS_StyleScopedClasses['search-box']} */ ;
let __VLS_10;
/** @ts-ignore @type { | typeof __VLS_components.Search} */
Search;
// @ts-ignore
const __VLS_11 = __VLS_asFunctionalComponent1(__VLS_10, new __VLS_10({}));
const __VLS_12 = __VLS_11({}, ...__VLS_functionalComponentArgsRest(__VLS_11));
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    placeholder: "搜索用户名或姓名",
});
(__VLS_ctx.query);
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
(__VLS_ctx.users.length);
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "user-table" },
});
/** @type {__VLS_StyleScopedClasses['user-table']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "table-head" },
});
/** @type {__VLS_StyleScopedClasses['table-head']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
for (const [user] of __VLS_vFor((__VLS_ctx.filtered))) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.article, __VLS_intrinsics.article)({
        key: (user.id),
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "user-name" },
    });
    /** @type {__VLS_StyleScopedClasses['user-name']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    (user.displayName?.slice(0, 1) || user.username.slice(0, 1));
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        ...{ onChange: (...[$event]) => {
                return (__VLS_ctx.save(user));
                // @ts-ignore
                [query, users, filtered, save,];
            } },
    });
    (user.displayName);
    __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
    (user.username);
    __VLS_asFunctionalElement1(__VLS_intrinsics.select, __VLS_intrinsics.select)({
        ...{ onChange: (...[$event]) => {
                return (__VLS_ctx.save(user));
                // @ts-ignore
                [save,];
            } },
        value: (user.role),
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.option, __VLS_intrinsics.option)({
        value: "ADMIN",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.option, __VLS_intrinsics.option)({
        value: "USER",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.toggle(user));
                // @ts-ignore
                [toggle,];
            } },
        ...{ class: "status-switch" },
        ...{ class: ({ enabled: user.enabled }) },
    });
    /** @type {__VLS_StyleScopedClasses['status-switch']} */ ;
    /** @type {__VLS_StyleScopedClasses['enabled']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.i, __VLS_intrinsics.i)({});
    (user.enabled ? '已启用' : '已禁用');
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "row-actions" },
    });
    /** @type {__VLS_StyleScopedClasses['row-actions']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.resetPassword(user));
                // @ts-ignore
                [resetPassword,];
            } },
        title: "重置密码",
    });
    let __VLS_15;
    /** @ts-ignore @type { | typeof __VLS_components.KeyRound} */
    KeyRound;
    // @ts-ignore
    const __VLS_16 = __VLS_asFunctionalComponent1(__VLS_15, new __VLS_15({}));
    const __VLS_17 = __VLS_16({}, ...__VLS_functionalComponentArgsRest(__VLS_16));
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.remove(user));
                // @ts-ignore
                [remove,];
            } },
        ...{ class: "remove" },
        title: "删除用户",
    });
    /** @type {__VLS_StyleScopedClasses['remove']} */ ;
    let __VLS_20;
    /** @ts-ignore @type { | typeof __VLS_components.Trash2} */
    Trash2;
    // @ts-ignore
    const __VLS_21 = __VLS_asFunctionalComponent1(__VLS_20, new __VLS_20({}));
    const __VLS_22 = __VLS_21({}, ...__VLS_functionalComponentArgsRest(__VLS_21));
    // @ts-ignore
    [];
}
if (!__VLS_ctx.filtered.length) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "no-users" },
    });
    /** @type {__VLS_StyleScopedClasses['no-users']} */ ;
    let __VLS_25;
    /** @ts-ignore @type { | typeof __VLS_components.UserCog} */
    UserCog;
    // @ts-ignore
    const __VLS_26 = __VLS_asFunctionalComponent1(__VLS_25, new __VLS_25({}));
    const __VLS_27 = __VLS_26({}, ...__VLS_functionalComponentArgsRest(__VLS_26));
}
if (__VLS_ctx.showCreate) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showCreate))
                    throw 0;
                return (__VLS_ctx.showCreate = false);
                // @ts-ignore
                [showCreate, showCreate, filtered,];
            } },
        ...{ class: "modal-backdrop" },
    });
    /** @type {__VLS_StyleScopedClasses['modal-backdrop']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.form, __VLS_intrinsics.form)({
        ...{ onSubmit: (__VLS_ctx.create) },
        ...{ class: "modal" },
    });
    /** @type {__VLS_StyleScopedClasses['modal']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showCreate))
                    throw 0;
                return (__VLS_ctx.showCreate = false);
                // @ts-ignore
                [showCreate, create,];
            } },
        type: "button",
        ...{ class: "modal-close" },
    });
    /** @type {__VLS_StyleScopedClasses['modal-close']} */ ;
    let __VLS_30;
    /** @ts-ignore @type { | typeof __VLS_components.X} */
    X;
    // @ts-ignore
    const __VLS_31 = __VLS_asFunctionalComponent1(__VLS_30, new __VLS_30({}));
    const __VLS_32 = __VLS_31({}, ...__VLS_functionalComponentArgsRest(__VLS_31));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "eyebrow" },
    });
    /** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        required: true,
        autocomplete: "off",
        placeholder: "登录账号",
    });
    (__VLS_ctx.form.username);
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        placeholder: "用户姓名",
    });
    (__VLS_ctx.form.displayName);
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        type: "password",
        required: true,
        minlength: "6",
        autocomplete: "new-password",
        placeholder: "至少 6 位",
    });
    (__VLS_ctx.form.password);
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.select, __VLS_intrinsics.select)({
        value: (__VLS_ctx.form.role),
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.option, __VLS_intrinsics.option)({
        value: "USER",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.option, __VLS_intrinsics.option)({
        value: "ADMIN",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ class: "primary" },
        disabled: (__VLS_ctx.busy),
    });
    /** @type {__VLS_StyleScopedClasses['primary']} */ ;
    let __VLS_35;
    /** @ts-ignore @type { | typeof __VLS_components.Check} */
    Check;
    // @ts-ignore
    const __VLS_36 = __VLS_asFunctionalComponent1(__VLS_35, new __VLS_35({}));
    const __VLS_37 = __VLS_36({}, ...__VLS_functionalComponentArgsRest(__VLS_36));
    (__VLS_ctx.busy ? '正在创建…' : '创建用户');
}
// @ts-ignore
[form, form, form, form, busy, busy,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
