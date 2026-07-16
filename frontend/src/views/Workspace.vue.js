import { computed, nextTick, onMounted, ref } from 'vue';
import { api } from '../api';
import Composer from '../components/Composer.vue';
import { Camera, ChevronDown, Database, LockKeyhole, LogOut, MessageSquare, Plus, Trash2, UserCog, X } from 'lucide-vue-next';
const messages = ref([]), conversations = ref([]), activeConversationId = ref(), pendingDeleteConversation = ref(), deletingConversation = ref(false), busy = ref(false);
const messagesContainer = ref();
const showAccount = ref(false), showProfileMenu = ref(false), avatarUrl = ref(localStorage.getItem('avatarUrl') || '');
const passwordForm = ref({ currentPassword: '', newPassword: '', confirmPassword: '' }), accountMessage = ref('');
const displayName = localStorage.getItem('name') || '知识库用户', isAdmin = localStorage.getItem('role') === 'ADMIN';
const activeConversation = computed(() => conversations.value.find(x => x.id === activeConversationId.value));
async function load() { conversations.value = (await api.get('/conversations')).data; }
async function newChat() { const { data } = await api.post('/conversations'); conversations.value = [data, ...conversations.value]; activeConversationId.value = data.id; messages.value = []; }
async function openConversation(c) { activeConversationId.value = c.id; messages.value = (await api.get(`/conversations/${c.id}/messages`)).data.map((m) => ({ id: m.id, role: m.role, text: m.content })); await scrollToBottom(); }
function requestDeleteConversation(c, e) { e.stopPropagation(); deletingConversation.value = false; pendingDeleteConversation.value = c; }
async function confirmDeleteConversation() { const conversation = pendingDeleteConversation.value; if (!conversation || deletingConversation.value)
    return; deletingConversation.value = true; try {
    await api.delete(`/conversations/${conversation.id}`);
    conversations.value = conversations.value.filter(x => x.id !== conversation.id);
    if (activeConversationId.value === conversation.id) {
        activeConversationId.value = undefined;
        messages.value = [];
    }
    pendingDeleteConversation.value = undefined;
}
finally {
    deletingConversation.value = false;
} }
async function ask(text) { messages.value.push({ role: 'user', text }); busy.value = true; await scrollToBottom(); try {
    const { data } = await api.post('/chat', { conversationId: activeConversationId.value, question: text });
    activeConversationId.value = data.conversationId;
    messages.value.push({ role: 'assistant', text: data.answer });
    await load();
}
catch (error) {
    messages.value.push({ role: 'assistant', text: error.response?.data?.message || '暂时无法连接 AI 服务，请稍后重试。' });
}
finally {
    busy.value = false;
    await scrollToBottom();
} }
async function scrollToBottom() { await nextTick(); const element = messagesContainer.value; if (element)
    element.scrollTo({ top: element.scrollHeight, behavior: 'smooth' }); }
function logout() { localStorage.clear(); location.href = '/login'; }
async function uploadAvatar(e) { const input = e.target, file = input.files?.[0]; if (!file)
    return; accountMessage.value = ''; if (file.size > 2 * 1024 * 1024) {
    accountMessage.value = '头像不能超过 2MB';
    return;
} const form = new FormData(); form.append('file', file); try {
    const { data } = await api.post('/profile/avatar', form);
    avatarUrl.value = data.url;
    localStorage.setItem('avatarUrl', data.url);
    accountMessage.value = '头像更新成功';
}
catch (error) {
    accountMessage.value = error.response?.data?.message || '头像上传失败';
}
finally {
    input.value = '';
} }
async function changePassword() { accountMessage.value = ''; if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    accountMessage.value = '两次输入的新密码不一致';
    return;
} try {
    await api.put('/profile/password', { currentPassword: passwordForm.value.currentPassword, newPassword: passwordForm.value.newPassword });
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' };
    accountMessage.value = '密码修改成功，下次登录请使用新密码';
}
catch (error) {
    accountMessage.value = error.response?.data?.message || '密码修改失败';
} }
onMounted(load);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['user']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['user']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['user']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['user']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['user']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['user']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['assistant']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['messages']} */ ;
/** @type {__VLS_StyleScopedClasses['module-entry']} */ ;
/** @type {__VLS_StyleScopedClasses['module-entry']} */ ;
/** @type {__VLS_StyleScopedClasses['conversation-nav']} */ ;
/** @type {__VLS_StyleScopedClasses['page-title']} */ ;
/** @type {__VLS_StyleScopedClasses['account-trigger']} */ ;
/** @type {__VLS_StyleScopedClasses['account-trigger']} */ ;
/** @type {__VLS_StyleScopedClasses['account-trigger']} */ ;
/** @type {__VLS_StyleScopedClasses['top-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['account-name']} */ ;
/** @type {__VLS_StyleScopedClasses['account-name']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['profile-menu']} */ ;
/** @type {__VLS_StyleScopedClasses['avatar-editor']} */ ;
/** @type {__VLS_StyleScopedClasses['avatar-editor']} */ ;
/** @type {__VLS_StyleScopedClasses['account-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['account-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['account-avatar']} */ ;
/** @type {__VLS_StyleScopedClasses['avatar-editor']} */ ;
/** @type {__VLS_StyleScopedClasses['avatar-editor']} */ ;
/** @type {__VLS_StyleScopedClasses['avatar-editor']} */ ;
/** @type {__VLS_StyleScopedClasses['password-form']} */ ;
/** @type {__VLS_StyleScopedClasses['password-form']} */ ;
/** @type {__VLS_StyleScopedClasses['password-form']} */ ;
/** @type {__VLS_StyleScopedClasses['account-name']} */ ;
/** @type {__VLS_StyleScopedClasses['topbar']} */ ;
/** @type {__VLS_StyleScopedClasses['page-title']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.main, __VLS_intrinsics.main)({
    ...{ class: "app-shell" },
});
/** @type {__VLS_StyleScopedClasses['app-shell']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.aside, __VLS_intrinsics.aside)({
    ...{ class: "sidebar" },
});
/** @type {__VLS_StyleScopedClasses['sidebar']} */ ;
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
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.newChat) },
    ...{ class: "new-chat" },
});
/** @type {__VLS_StyleScopedClasses['new-chat']} */ ;
let __VLS_0;
/** @ts-ignore @type { | typeof __VLS_components.Plus} */
Plus;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({}));
const __VLS_2 = __VLS_1({}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            return (__VLS_ctx.$router.push('/knowledge'));
            // @ts-ignore
            [newChat, $router,];
        } },
    ...{ class: "module-entry" },
});
/** @type {__VLS_StyleScopedClasses['module-entry']} */ ;
let __VLS_5;
/** @ts-ignore @type { | typeof __VLS_components.Database} */
Database;
// @ts-ignore
const __VLS_6 = __VLS_asFunctionalComponent1(__VLS_5, new __VLS_5({}));
const __VLS_7 = __VLS_6({}, ...__VLS_functionalComponentArgsRest(__VLS_6));
if (__VLS_ctx.isAdmin) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.isAdmin))
                    throw 0;
                return (__VLS_ctx.$router.push('/users'));
                // @ts-ignore
                [$router, isAdmin,];
            } },
        ...{ class: "module-entry" },
    });
    /** @type {__VLS_StyleScopedClasses['module-entry']} */ ;
    let __VLS_10;
    /** @ts-ignore @type { | typeof __VLS_components.UserCog} */
    UserCog;
    // @ts-ignore
    const __VLS_11 = __VLS_asFunctionalComponent1(__VLS_10, new __VLS_10({}));
    const __VLS_12 = __VLS_11({}, ...__VLS_functionalComponentArgsRest(__VLS_11));
}
__VLS_asFunctionalElement1(__VLS_intrinsics.nav, __VLS_intrinsics.nav)({
    ...{ class: "conversation-nav" },
});
/** @type {__VLS_StyleScopedClasses['conversation-nav']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "nav-title" },
});
/** @type {__VLS_StyleScopedClasses['nav-title']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
(__VLS_ctx.conversations.length);
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "conversation-list" },
});
/** @type {__VLS_StyleScopedClasses['conversation-list']} */ ;
for (const [c] of __VLS_vFor((__VLS_ctx.conversations))) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.openConversation(c));
                // @ts-ignore
                [conversations, conversations, openConversation,];
            } },
        key: (c.id),
        ...{ class: "conversation-item" },
        ...{ class: ({ active: __VLS_ctx.activeConversationId === c.id }) },
    });
    /** @type {__VLS_StyleScopedClasses['conversation-item']} */ ;
    /** @type {__VLS_StyleScopedClasses['active']} */ ;
    let __VLS_15;
    /** @ts-ignore @type { | typeof __VLS_components.MessageSquare} */
    MessageSquare;
    // @ts-ignore
    const __VLS_16 = __VLS_asFunctionalComponent1(__VLS_15, new __VLS_15({}));
    const __VLS_17 = __VLS_16({}, ...__VLS_functionalComponentArgsRest(__VLS_16));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    (c.title);
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.requestDeleteConversation(c, $event));
                // @ts-ignore
                [activeConversationId, requestDeleteConversation,];
            } },
        type: "button",
        title: "删除对话",
    });
    let __VLS_20;
    /** @ts-ignore @type { | typeof __VLS_components.Trash2} */
    Trash2;
    // @ts-ignore
    const __VLS_21 = __VLS_asFunctionalComponent1(__VLS_20, new __VLS_20({}));
    const __VLS_22 = __VLS_21({}, ...__VLS_functionalComponentArgsRest(__VLS_21));
    // @ts-ignore
    [];
}
if (!__VLS_ctx.conversations.length) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
        ...{ class: "conversation-empty" },
    });
    /** @type {__VLS_StyleScopedClasses['conversation-empty']} */ ;
}
__VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
    ...{ class: "workspace" },
});
/** @type {__VLS_StyleScopedClasses['workspace']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.header, __VLS_intrinsics.header)({
    ...{ class: "topbar" },
});
/** @type {__VLS_StyleScopedClasses['topbar']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "page-title" },
});
/** @type {__VLS_StyleScopedClasses['page-title']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
if (__VLS_ctx.activeConversation) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    (__VLS_ctx.activeConversation.title);
}
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "top-actions" },
});
/** @type {__VLS_StyleScopedClasses['top-actions']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "knowledge-status" },
});
/** @type {__VLS_StyleScopedClasses['knowledge-status']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ onClick: () => { } },
    ...{ class: "account-trigger" },
});
/** @type {__VLS_StyleScopedClasses['account-trigger']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            return (__VLS_ctx.showProfileMenu = !__VLS_ctx.showProfileMenu);
            // @ts-ignore
            [conversations, activeConversation, activeConversation, showProfileMenu, showProfileMenu,];
        } },
    type: "button",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "top-avatar" },
});
/** @type {__VLS_StyleScopedClasses['top-avatar']} */ ;
if (__VLS_ctx.avatarUrl) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.img)({
        src: (__VLS_ctx.avatarUrl),
        alt: "用户头像",
    });
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    (__VLS_ctx.displayName.slice(0, 1));
}
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "account-name" },
});
/** @type {__VLS_StyleScopedClasses['account-name']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
(__VLS_ctx.displayName);
__VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
(__VLS_ctx.isAdmin ? '系统管理员' : '普通用户');
let __VLS_25;
/** @ts-ignore @type { | typeof __VLS_components.ChevronDown} */
ChevronDown;
// @ts-ignore
const __VLS_26 = __VLS_asFunctionalComponent1(__VLS_25, new __VLS_25({}));
const __VLS_27 = __VLS_26({}, ...__VLS_functionalComponentArgsRest(__VLS_26));
if (__VLS_ctx.showProfileMenu) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: () => { } },
        ...{ class: "profile-menu" },
    });
    /** @type {__VLS_StyleScopedClasses['profile-menu']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showProfileMenu))
                    throw 0;
                __VLS_ctx.showAccount = true;
                __VLS_ctx.showProfileMenu = false;
                // @ts-ignore
                [isAdmin, showProfileMenu, showProfileMenu, avatarUrl, avatarUrl, displayName, displayName, showAccount,];
            } },
        type: "button",
    });
    let __VLS_30;
    /** @ts-ignore @type { | typeof __VLS_components.UserCog} */
    UserCog;
    // @ts-ignore
    const __VLS_31 = __VLS_asFunctionalComponent1(__VLS_30, new __VLS_30({}));
    const __VLS_32 = __VLS_31({}, ...__VLS_functionalComponentArgsRest(__VLS_31));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (__VLS_ctx.logout) },
        type: "button",
        ...{ class: "logout-action" },
    });
    /** @type {__VLS_StyleScopedClasses['logout-action']} */ ;
    let __VLS_35;
    /** @ts-ignore @type { | typeof __VLS_components.LogOut} */
    LogOut;
    // @ts-ignore
    const __VLS_36 = __VLS_asFunctionalComponent1(__VLS_35, new __VLS_35({}));
    const __VLS_37 = __VLS_36({}, ...__VLS_functionalComponentArgsRest(__VLS_36));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
}
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "chat" },
});
/** @type {__VLS_StyleScopedClasses['chat']} */ ;
if (!__VLS_ctx.messages.length) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "welcome" },
    });
    /** @type {__VLS_StyleScopedClasses['welcome']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "eyebrow" },
    });
    /** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h1, __VLS_intrinsics.h1)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ref: "messagesContainer",
        ...{ class: "messages" },
    });
    /** @type {__VLS_StyleScopedClasses['messages']} */ ;
    for (const [m, i] of __VLS_vFor((__VLS_ctx.messages))) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.article, __VLS_intrinsics.article)({
            key: (m.id || i),
            ...{ class: (m.role) },
        });
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "msg-avatar" },
        });
        /** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
        if (m.role === 'user') {
            __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
        }
        else {
            __VLS_asFunctionalElement1(__VLS_intrinsics.img)({
                src: "/ggbond-logo.jpg",
                alt: "GGBOND AI",
            });
        }
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
        (m.role === 'user' ? '你' : 'GGBOND AI');
        __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
        (m.text);
        // @ts-ignore
        [logout, messages, messages,];
    }
    if (__VLS_ctx.busy) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.article, __VLS_intrinsics.article)({
            ...{ class: "assistant" },
        });
        /** @type {__VLS_StyleScopedClasses['assistant']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "msg-avatar" },
        });
        /** @type {__VLS_StyleScopedClasses['msg-avatar']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.img)({
            src: "/ggbond-logo.jpg",
            alt: "GGBOND AI",
        });
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    }
}
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "composer-wrap" },
});
/** @type {__VLS_StyleScopedClasses['composer-wrap']} */ ;
const __VLS_40 = Composer;
// @ts-ignore
const __VLS_41 = __VLS_asFunctionalComponent1(__VLS_40, new __VLS_40({
    ...{ 'onSend': {} },
    disabled: (__VLS_ctx.busy),
}));
const __VLS_42 = __VLS_41({
    ...{ 'onSend': {} },
    disabled: (__VLS_ctx.busy),
}, ...__VLS_functionalComponentArgsRest(__VLS_41));
let __VLS_45;
const __VLS_46 = {
    /** @type {typeof __VLS_45.send} */
    onSend: (__VLS_ctx.ask),
};
var __VLS_43;
var __VLS_44;
__VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
if (__VLS_ctx.showAccount) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showAccount))
                    throw 0;
                return (__VLS_ctx.showAccount = false);
                // @ts-ignore
                [showAccount, showAccount, busy, busy, ask,];
            } },
        ...{ class: "modal-backdrop" },
    });
    /** @type {__VLS_StyleScopedClasses['modal-backdrop']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
        ...{ class: "modal account-modal" },
    });
    /** @type {__VLS_StyleScopedClasses['modal']} */ ;
    /** @type {__VLS_StyleScopedClasses['account-modal']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showAccount))
                    throw 0;
                return (__VLS_ctx.showAccount = false);
                // @ts-ignore
                [showAccount,];
            } },
        ...{ class: "modal-close" },
    });
    /** @type {__VLS_StyleScopedClasses['modal-close']} */ ;
    let __VLS_47;
    /** @ts-ignore @type { | typeof __VLS_components.X} */
    X;
    // @ts-ignore
    const __VLS_48 = __VLS_asFunctionalComponent1(__VLS_47, new __VLS_47({}));
    const __VLS_49 = __VLS_48({}, ...__VLS_functionalComponentArgsRest(__VLS_48));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "eyebrow" },
    });
    /** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "avatar-editor" },
    });
    /** @type {__VLS_StyleScopedClasses['avatar-editor']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "account-avatar" },
    });
    /** @type {__VLS_StyleScopedClasses['account-avatar']} */ ;
    if (__VLS_ctx.avatarUrl) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.img)({
            src: (__VLS_ctx.avatarUrl),
            alt: "头像",
        });
    }
    else {
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
        (__VLS_ctx.displayName.slice(0, 1));
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.i, __VLS_intrinsics.i)({});
    let __VLS_52;
    /** @ts-ignore @type { | typeof __VLS_components.Camera} */
    Camera;
    // @ts-ignore
    const __VLS_53 = __VLS_asFunctionalComponent1(__VLS_52, new __VLS_52({}));
    const __VLS_54 = __VLS_53({}, ...__VLS_functionalComponentArgsRest(__VLS_53));
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        ...{ onChange: (__VLS_ctx.uploadAvatar) },
        type: "file",
        accept: "image/jpeg,image/png,image/webp",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
    (__VLS_ctx.displayName);
    __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
    (__VLS_ctx.isAdmin ? '系统管理员' : '普通用户');
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "account-divider" },
    });
    /** @type {__VLS_StyleScopedClasses['account-divider']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.form, __VLS_intrinsics.form)({
        ...{ onSubmit: (__VLS_ctx.changePassword) },
        ...{ class: "password-form" },
    });
    /** @type {__VLS_StyleScopedClasses['password-form']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h3, __VLS_intrinsics.h3)({});
    let __VLS_57;
    /** @ts-ignore @type { | typeof __VLS_components.LockKeyhole} */
    LockKeyhole;
    // @ts-ignore
    const __VLS_58 = __VLS_asFunctionalComponent1(__VLS_57, new __VLS_57({}));
    const __VLS_59 = __VLS_58({}, ...__VLS_functionalComponentArgsRest(__VLS_58));
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        type: "password",
        required: true,
    });
    (__VLS_ctx.passwordForm.currentPassword);
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        type: "password",
        required: true,
        minlength: "6",
    });
    (__VLS_ctx.passwordForm.newPassword);
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        type: "password",
        required: true,
        minlength: "6",
    });
    (__VLS_ctx.passwordForm.confirmPassword);
    if (__VLS_ctx.accountMessage) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
            ...{ class: "account-message" },
        });
        /** @type {__VLS_StyleScopedClasses['account-message']} */ ;
        (__VLS_ctx.accountMessage);
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ class: "primary" },
    });
    /** @type {__VLS_StyleScopedClasses['primary']} */ ;
}
if (__VLS_ctx.pendingDeleteConversation) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.pendingDeleteConversation))
                    throw 0;
                return (__VLS_ctx.pendingDeleteConversation = undefined);
                // @ts-ignore
                [isAdmin, avatarUrl, avatarUrl, displayName, displayName, uploadAvatar, changePassword, passwordForm, passwordForm, passwordForm, accountMessage, accountMessage, pendingDeleteConversation, pendingDeleteConversation,];
            } },
        ...{ class: "modal-backdrop" },
    });
    /** @type {__VLS_StyleScopedClasses['modal-backdrop']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
        ...{ class: "conversation-delete-modal" },
    });
    /** @type {__VLS_StyleScopedClasses['conversation-delete-modal']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    let __VLS_62;
    /** @ts-ignore @type { | typeof __VLS_components.Trash2} */
    Trash2;
    // @ts-ignore
    const __VLS_63 = __VLS_asFunctionalComponent1(__VLS_62, new __VLS_62({}));
    const __VLS_64 = __VLS_63({}, ...__VLS_functionalComponentArgsRest(__VLS_63));
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.pendingDeleteConversation.title);
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.pendingDeleteConversation))
                    throw 0;
                return (__VLS_ctx.pendingDeleteConversation = undefined);
                // @ts-ignore
                [pendingDeleteConversation, pendingDeleteConversation,];
            } },
        type: "button",
        disabled: (__VLS_ctx.deletingConversation),
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (__VLS_ctx.confirmDeleteConversation) },
        type: "button",
        ...{ class: "confirm" },
        disabled: (__VLS_ctx.deletingConversation),
    });
    /** @type {__VLS_StyleScopedClasses['confirm']} */ ;
    (__VLS_ctx.deletingConversation ? '正在删除…' : '删除对话');
}
// @ts-ignore
[deletingConversation, deletingConversation, deletingConversation, confirmDeleteConversation,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
