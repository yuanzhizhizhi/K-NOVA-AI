import { computed, onMounted, ref } from 'vue';
import { api } from '../api';
import Composer from '../components/Composer.vue';
import { BookOpen, Plus, Search, FileText, Trash2, UploadCloud, Database, LogOut, X, MessageSquare } from 'lucide-vue-next';
const bases = ref([]), selected = ref(), docs = ref([]), messages = ref([]), conversations = ref([]);
const activeConversationId = ref(), busy = ref(false), showCreate = ref(false), showDocs = ref(false), query = ref('');
const newBase = ref({ name: '', description: '', color: '#6D5EF5' }), displayName = localStorage.getItem('name') || '知识库管理员';
const filtered = computed(() => bases.value.filter(x => x.name.toLowerCase().includes(query.value.toLowerCase())));
async function load() { bases.value = (await api.get('/knowledge-bases')).data; if (!selected.value && bases.value[0])
    await selectBase(bases.value[0]); }
async function selectBase(k) { selected.value = k; newChat(); const [d, c] = await Promise.all([api.get(`/knowledge-bases/${k.id}/documents`), api.get(`/knowledge-bases/${k.id}/conversations`)]); docs.value = d.data; conversations.value = c.data; }
function newChat() { activeConversationId.value = undefined; messages.value = []; }
async function openConversation(c) { activeConversationId.value = c.id; const { data } = await api.get(`/conversations/${c.id}/messages`); messages.value = data.map((m) => ({ id: m.id, role: m.role, text: m.content })); }
async function deleteConversation(c, e) { e.stopPropagation(); if (!confirm(`确认删除对话“${c.title}”吗？`))
    return; await api.delete(`/conversations/${c.id}`); conversations.value = conversations.value.filter(x => x.id !== c.id); if (activeConversationId.value === c.id)
    newChat(); }
async function create() { const { data } = await api.post('/knowledge-bases', newBase.value); showCreate.value = false; newBase.value = { name: '', description: '', color: '#6D5EF5' }; await load(); await selectBase(data); }
async function upload(e) { const input = e.target, file = input.files?.[0]; if (!file || !selected.value)
    return; const form = new FormData(); form.append('file', file); busy.value = true; try {
    await api.post(`/knowledge-bases/${selected.value.id}/documents`, form);
    await selectBase(selected.value);
    await load();
}
finally {
    busy.value = false;
    input.value = '';
} }
async function removeDoc(d) { if (!confirm(`删除“${d.fileName}”及其全部向量？`))
    return; await api.delete(`/documents/${d.id}`); await selectBase(selected.value); await load(); }
async function ask(text) { if (!selected.value)
    return; messages.value.push({ role: 'user', text }); busy.value = true; try {
    const { data } = await api.post(`/knowledge-bases/${selected.value.id}/chat`, { conversationId: activeConversationId.value, question: text });
    activeConversationId.value = data.conversationId;
    messages.value.push({ role: 'assistant', text: data.answer });
    conversations.value = (await api.get(`/knowledge-bases/${selected.value.id}/conversations`)).data;
}
catch {
    messages.value.push({ role: 'assistant', text: '暂时无法连接 AI 服务，请检查模型和向量库配置。' });
}
finally {
    busy.value = false;
} }
function logout() { localStorage.clear(); location.href = '/login'; }
onMounted(load);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.main, __VLS_intrinsics.main)({
    ...{ class: "app-shell" },
});
/** @type {__VLS_StyleScopedClasses['app-shell']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.aside, __VLS_intrinsics.aside)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "brand" },
});
/** @type {__VLS_StyleScopedClasses['brand']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "brand-mark" },
});
/** @type {__VLS_StyleScopedClasses['brand-mark']} */ ;
let __VLS_0;
/** @ts-ignore @type { | typeof __VLS_components.BookOpen} */
BookOpen;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
    size: (20),
}));
const __VLS_2 = __VLS_1({
    size: (20),
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.newChat) },
    ...{ class: "new-chat" },
});
/** @type {__VLS_StyleScopedClasses['new-chat']} */ ;
let __VLS_5;
/** @ts-ignore @type { | typeof __VLS_components.Plus} */
Plus;
// @ts-ignore
const __VLS_6 = __VLS_asFunctionalComponent1(__VLS_5, new __VLS_5({}));
const __VLS_7 = __VLS_6({}, ...__VLS_functionalComponentArgsRest(__VLS_6));
__VLS_asFunctionalElement1(__VLS_intrinsics.nav, __VLS_intrinsics.nav)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "nav-title" },
});
/** @type {__VLS_StyleScopedClasses['nav-title']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            return (__VLS_ctx.showCreate = true);
            // @ts-ignore
            [newChat, showCreate,];
        } },
});
let __VLS_10;
/** @ts-ignore @type { | typeof __VLS_components.Plus} */
Plus;
// @ts-ignore
const __VLS_11 = __VLS_asFunctionalComponent1(__VLS_10, new __VLS_10({}));
const __VLS_12 = __VLS_11({}, ...__VLS_functionalComponentArgsRest(__VLS_11));
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "search" },
});
/** @type {__VLS_StyleScopedClasses['search']} */ ;
let __VLS_15;
/** @ts-ignore @type { | typeof __VLS_components.Search} */
Search;
// @ts-ignore
const __VLS_16 = __VLS_asFunctionalComponent1(__VLS_15, new __VLS_15({}));
const __VLS_17 = __VLS_16({}, ...__VLS_functionalComponentArgsRest(__VLS_16));
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    placeholder: "搜索知识库",
});
(__VLS_ctx.query);
for (const [k] of __VLS_vFor((__VLS_ctx.filtered))) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.selectBase(k));
                // @ts-ignore
                [query, filtered, selectBase,];
            } },
        key: (k.id),
        ...{ class: "kb-item" },
        ...{ class: ({ active: __VLS_ctx.selected?.id === k.id }) },
    });
    /** @type {__VLS_StyleScopedClasses['kb-item']} */ ;
    /** @type {__VLS_StyleScopedClasses['active']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "kb-dot" },
        ...{ style: ({ background: k.color }) },
    });
    /** @type {__VLS_StyleScopedClasses['kb-dot']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
    (k.name);
    __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
    (k.documents);
    // @ts-ignore
    [selected,];
}
if (__VLS_ctx.selected) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "nav-title conversation-title" },
    });
    /** @type {__VLS_StyleScopedClasses['nav-title']} */ ;
    /** @type {__VLS_StyleScopedClasses['conversation-title']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
    (__VLS_ctx.conversations.length);
}
for (const [c] of __VLS_vFor((__VLS_ctx.conversations))) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.openConversation(c));
                // @ts-ignore
                [selected, conversations, conversations, openConversation,];
            } },
        key: (c.id),
        ...{ class: "conversation-item" },
        ...{ class: ({ active: __VLS_ctx.activeConversationId === c.id }) },
    });
    /** @type {__VLS_StyleScopedClasses['conversation-item']} */ ;
    /** @type {__VLS_StyleScopedClasses['active']} */ ;
    let __VLS_20;
    /** @ts-ignore @type { | typeof __VLS_components.MessageSquare} */
    MessageSquare;
    // @ts-ignore
    const __VLS_21 = __VLS_asFunctionalComponent1(__VLS_20, new __VLS_20({}));
    const __VLS_22 = __VLS_21({}, ...__VLS_functionalComponentArgsRest(__VLS_21));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    (c.title);
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.deleteConversation(c, $event));
                // @ts-ignore
                [activeConversationId, deleteConversation,];
            } },
        title: "删除对话",
    });
    let __VLS_25;
    /** @ts-ignore @type { | typeof __VLS_components.Trash2} */
    Trash2;
    // @ts-ignore
    const __VLS_26 = __VLS_asFunctionalComponent1(__VLS_25, new __VLS_25({}));
    const __VLS_27 = __VLS_26({}, ...__VLS_functionalComponentArgsRest(__VLS_26));
    // @ts-ignore
    [];
}
if (__VLS_ctx.selected && !__VLS_ctx.conversations.length) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
        ...{ class: "conversation-empty" },
    });
    /** @type {__VLS_StyleScopedClasses['conversation-empty']} */ ;
}
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "profile" },
});
/** @type {__VLS_StyleScopedClasses['profile']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "avatar" },
});
/** @type {__VLS_StyleScopedClasses['avatar']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
(__VLS_ctx.displayName);
__VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.logout) },
});
let __VLS_30;
/** @ts-ignore @type { | typeof __VLS_components.LogOut} */
LogOut;
// @ts-ignore
const __VLS_31 = __VLS_asFunctionalComponent1(__VLS_30, new __VLS_30({}));
const __VLS_32 = __VLS_31({}, ...__VLS_functionalComponentArgsRest(__VLS_31));
__VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
    ...{ class: "workspace" },
});
/** @type {__VLS_StyleScopedClasses['workspace']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.header, __VLS_intrinsics.header)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "crumb" },
});
/** @type {__VLS_StyleScopedClasses['crumb']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
(__VLS_ctx.selected?.name || '请选择知识库');
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            return (__VLS_ctx.showDocs = true);
            // @ts-ignore
            [selected, selected, conversations, displayName, logout, showDocs,];
        } },
    ...{ class: "secondary" },
    disabled: (!__VLS_ctx.selected),
});
/** @type {__VLS_StyleScopedClasses['secondary']} */ ;
let __VLS_35;
/** @ts-ignore @type { | typeof __VLS_components.Database} */
Database;
// @ts-ignore
const __VLS_36 = __VLS_asFunctionalComponent1(__VLS_35, new __VLS_35({}));
const __VLS_37 = __VLS_36({}, ...__VLS_functionalComponentArgsRest(__VLS_36));
if (!__VLS_ctx.selected) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "empty" },
    });
    /** @type {__VLS_StyleScopedClasses['empty']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "empty-icon" },
    });
    /** @type {__VLS_StyleScopedClasses['empty-icon']} */ ;
    let __VLS_40;
    /** @ts-ignore @type { | typeof __VLS_components.BookOpen} */
    BookOpen;
    // @ts-ignore
    const __VLS_41 = __VLS_asFunctionalComponent1(__VLS_40, new __VLS_40({}));
    const __VLS_42 = __VLS_41({}, ...__VLS_functionalComponentArgsRest(__VLS_41));
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(!__VLS_ctx.selected))
                    throw 0;
                return (__VLS_ctx.showCreate = true);
                // @ts-ignore
                [showCreate, selected, selected,];
            } },
        ...{ class: "primary" },
    });
    /** @type {__VLS_StyleScopedClasses['primary']} */ ;
    let __VLS_45;
    /** @ts-ignore @type { | typeof __VLS_components.Plus} */
    Plus;
    // @ts-ignore
    const __VLS_46 = __VLS_asFunctionalComponent1(__VLS_45, new __VLS_45({}));
    const __VLS_47 = __VLS_46({}, ...__VLS_functionalComponentArgsRest(__VLS_46));
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "chat" },
    });
    /** @type {__VLS_StyleScopedClasses['chat']} */ ;
    if (!__VLS_ctx.messages.length) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "welcome" },
        });
        /** @type {__VLS_StyleScopedClasses['welcome']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "spark" },
        });
        /** @type {__VLS_StyleScopedClasses['spark']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "eyebrow" },
        });
        /** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
        (__VLS_ctx.selected.name);
        __VLS_asFunctionalElement1(__VLS_intrinsics.h1, __VLS_intrinsics.h1)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
        (__VLS_ctx.selected.description || '我会基于知识库内容回答，并标注信息来源。');
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "suggestions" },
        });
        /** @type {__VLS_StyleScopedClasses['suggestions']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(!__VLS_ctx.selected))
                        throw 0;
                    if (!(!__VLS_ctx.messages.length))
                        throw 0;
                    return (__VLS_ctx.ask('请概括这个知识库包含的主要内容'));
                    // @ts-ignore
                    [selected, selected, messages, ask,];
                } },
        });
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(!__VLS_ctx.selected))
                        throw 0;
                    if (!(!__VLS_ctx.messages.length))
                        throw 0;
                    return (__VLS_ctx.ask('有哪些关键流程或注意事项？'));
                    // @ts-ignore
                    [ask,];
                } },
        });
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(!__VLS_ctx.selected))
                        throw 0;
                    if (!(!__VLS_ctx.messages.length))
                        throw 0;
                    return (__VLS_ctx.showDocs = true);
                    // @ts-ignore
                    [showDocs,];
                } },
        });
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    }
    else {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
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
            (m.role === 'user' ? '我' : '✦');
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
            __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
            (m.role === 'user' ? '你' : 'K·NOVA');
            __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
            (m.text);
            // @ts-ignore
            [messages,];
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
            __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
            __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
            __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({
                ...{ class: "thinking" },
            });
            /** @type {__VLS_StyleScopedClasses['thinking']} */ ;
        }
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "composer-wrap" },
    });
    /** @type {__VLS_StyleScopedClasses['composer-wrap']} */ ;
    const __VLS_50 = Composer;
    // @ts-ignore
    const __VLS_51 = __VLS_asFunctionalComponent1(__VLS_50, new __VLS_50({
        ...{ 'onSend': {} },
        disabled: (__VLS_ctx.busy),
    }));
    const __VLS_52 = __VLS_51({
        ...{ 'onSend': {} },
        disabled: (__VLS_ctx.busy),
    }, ...__VLS_functionalComponentArgsRest(__VLS_51));
    let __VLS_55;
    const __VLS_56 = {
        /** @type {typeof __VLS_55.send} */
        onSend: (__VLS_ctx.ask),
    };
    var __VLS_53;
    var __VLS_54;
    __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
}
if (__VLS_ctx.showCreate) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showCreate))
                    throw 0;
                return (__VLS_ctx.showCreate = false);
                // @ts-ignore
                [showCreate, showCreate, ask, busy, busy,];
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
    let __VLS_57;
    /** @ts-ignore @type { | typeof __VLS_components.X} */
    X;
    // @ts-ignore
    const __VLS_58 = __VLS_asFunctionalComponent1(__VLS_57, new __VLS_57({}));
    const __VLS_59 = __VLS_58({}, ...__VLS_functionalComponentArgsRest(__VLS_58));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "eyebrow" },
    });
    /** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        required: true,
        placeholder: "例如：产品研发手册",
    });
    (__VLS_ctx.newBase.name);
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.textarea, __VLS_intrinsics.textarea)({
        value: (__VLS_ctx.newBase.description),
        placeholder: "这个知识库主要用于…",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "colors" },
    });
    /** @type {__VLS_StyleScopedClasses['colors']} */ ;
    for (const [c] of __VLS_vFor((['#6D5EF5', '#00A884', '#E68A3F', '#DE5B79', '#3478F6']))) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.showCreate))
                        throw 0;
                    return (__VLS_ctx.newBase.color = c);
                    // @ts-ignore
                    [newBase, newBase, newBase,];
                } },
            key: (c),
            type: "button",
            ...{ style: ({ background: c }) },
            ...{ class: ({ chosen: __VLS_ctx.newBase.color === c }) },
        });
        /** @type {__VLS_StyleScopedClasses['chosen']} */ ;
        // @ts-ignore
        [newBase,];
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ class: "primary" },
        disabled: (!__VLS_ctx.newBase.name),
    });
    /** @type {__VLS_StyleScopedClasses['primary']} */ ;
}
if (__VLS_ctx.showDocs) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showDocs))
                    throw 0;
                return (__VLS_ctx.showDocs = false);
                // @ts-ignore
                [showDocs, showDocs, newBase,];
            } },
        ...{ class: "drawer-backdrop" },
    });
    /** @type {__VLS_StyleScopedClasses['drawer-backdrop']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
        ...{ class: "drawer" },
    });
    /** @type {__VLS_StyleScopedClasses['drawer']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.header, __VLS_intrinsics.header)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "eyebrow" },
    });
    /** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.selected?.name);
    (__VLS_ctx.docs.length);
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showDocs))
                    throw 0;
                return (__VLS_ctx.showDocs = false);
                // @ts-ignore
                [selected, showDocs, docs,];
            } },
        ...{ class: "modal-close" },
    });
    /** @type {__VLS_StyleScopedClasses['modal-close']} */ ;
    let __VLS_62;
    /** @ts-ignore @type { | typeof __VLS_components.X} */
    X;
    // @ts-ignore
    const __VLS_63 = __VLS_asFunctionalComponent1(__VLS_62, new __VLS_62({}));
    const __VLS_64 = __VLS_63({}, ...__VLS_functionalComponentArgsRest(__VLS_63));
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({
        ...{ class: "upload" },
        ...{ class: ({ loading: __VLS_ctx.busy }) },
    });
    /** @type {__VLS_StyleScopedClasses['upload']} */ ;
    /** @type {__VLS_StyleScopedClasses['loading']} */ ;
    let __VLS_67;
    /** @ts-ignore @type { | typeof __VLS_components.UploadCloud} */
    UploadCloud;
    // @ts-ignore
    const __VLS_68 = __VLS_asFunctionalComponent1(__VLS_67, new __VLS_67({}));
    const __VLS_69 = __VLS_68({}, ...__VLS_functionalComponentArgsRest(__VLS_68));
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
    (__VLS_ctx.busy ? '正在解析并向量化…' : '上传文档');
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        ...{ onChange: (__VLS_ctx.upload) },
        type: "file",
        accept: ".pdf,.doc,.docx,.ppt,.pptx,.txt,.md",
        disabled: (__VLS_ctx.busy),
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "doc-list" },
    });
    /** @type {__VLS_StyleScopedClasses['doc-list']} */ ;
    for (const [d] of __VLS_vFor((__VLS_ctx.docs))) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.article, __VLS_intrinsics.article)({
            key: (d.id),
        });
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "file-icon" },
        });
        /** @type {__VLS_StyleScopedClasses['file-icon']} */ ;
        let __VLS_72;
        /** @ts-ignore @type { | typeof __VLS_components.FileText} */
        FileText;
        // @ts-ignore
        const __VLS_73 = __VLS_asFunctionalComponent1(__VLS_72, new __VLS_72({}));
        const __VLS_74 = __VLS_73({}, ...__VLS_functionalComponentArgsRest(__VLS_73));
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
        (d.fileName);
        __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
        ((d.size / 1024).toFixed(1));
        (d.segmentCount);
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "status" },
            ...{ class: (d.status.toLowerCase()) },
        });
        /** @type {__VLS_StyleScopedClasses['status']} */ ;
        (d.status);
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!(__VLS_ctx.showDocs))
                        throw 0;
                    return (__VLS_ctx.removeDoc(d));
                    // @ts-ignore
                    [busy, busy, busy, docs, upload, removeDoc,];
                } },
        });
        let __VLS_77;
        /** @ts-ignore @type { | typeof __VLS_components.Trash2} */
        Trash2;
        // @ts-ignore
        const __VLS_78 = __VLS_asFunctionalComponent1(__VLS_77, new __VLS_77({}));
        const __VLS_79 = __VLS_78({}, ...__VLS_functionalComponentArgsRest(__VLS_78));
        // @ts-ignore
        [];
    }
    if (!__VLS_ctx.docs.length) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "no-docs" },
        });
        /** @type {__VLS_StyleScopedClasses['no-docs']} */ ;
    }
}
// @ts-ignore
[docs,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
