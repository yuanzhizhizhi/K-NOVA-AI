import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, BookOpen, CheckCircle2, FileText, FolderPlus, Plus, Search, Trash2, UploadCloud, X, XCircle } from 'lucide-vue-next';
import { api } from '../api';
import { appDialog } from '../components/appDialog';
const router = useRouter(), bases = ref([]), selected = ref(), docs = ref([]), query = ref(''), showCreate = ref(false), pendingDeleteBase = ref(), deletingBase = ref(false), uploading = ref(false), uploadProgress = ref({ done: 0, total: 0 });
const form = ref({ name: '', description: '', color: '#7157F6' });
const filtered = computed(() => bases.value.filter(k => !k.archived && k.name.toLowerCase().includes(query.value.toLowerCase())));
const totalDocs = computed(() => bases.value.reduce((sum, k) => sum + k.documents, 0));
function errorText(error, fallback) { return error.response?.data?.message || error.response?.data?.detail || fallback; }
async function load() { bases.value = (await api.get('/knowledge-bases')).data; const current = selected.value && bases.value.find(k => k.id === selected.value.id); if (current)
    await select(current);
else if (filtered.value[0])
    await select(filtered.value[0]);
else {
    selected.value = undefined;
    docs.value = [];
} }
async function select(base) { selected.value = base; docs.value = (await api.get(`/knowledge-bases/${base.id}/documents`)).data; }
async function create() { try {
    const { data } = await api.post('/knowledge-bases', form.value);
    showCreate.value = false;
    form.value = { name: '', description: '', color: '#7157F6' };
    await load();
    const created = bases.value.find(k => k.id === data.id);
    if (created)
        await select(created);
}
catch (error) {
    await appDialog.alert({ title: '创建失败', message: errorText(error, '无法创建知识库，请稍后重试。') });
} }
async function uploadMany(e) { const input = e.target, files = Array.from(input.files || []); if (!files.length || !selected.value)
    return; uploading.value = true; uploadProgress.value = { done: 0, total: files.length }; let failed = 0; try {
    for (const file of files) {
        const data = new FormData();
        data.append('file', file);
        try {
            await api.post(`/knowledge-bases/${selected.value.id}/documents`, data);
        }
        catch {
            failed++;
        }
        finally {
            uploadProgress.value.done++;
        }
    }
    await load();
    await appDialog.alert({ title: failed ? '上传部分完成' : '上传完成', message: failed ? `${files.length - failed} 个文件上传成功，${failed} 个文件处理失败。` : `${files.length} 个文件已上传并提交处理。` });
}
finally {
    uploading.value = false;
    input.value = '';
} }
async function removeDocument(doc) { if (!await appDialog.confirm({ title: '删除知识文档', message: `“${doc.fileName}”及其全部向量片段将被永久删除。`, confirmText: '删除文档', danger: true }))
    return; try {
    await api.delete(`/documents/${doc.id}`);
    await load();
    await appDialog.alert({ title: '删除成功', message: `文档“${doc.fileName}”已删除。` });
}
catch (error) {
    await appDialog.alert({ title: '删除失败', message: errorText(error, '无法删除该文档。') });
} }
function requestRemoveBase(base) { deletingBase.value = false; pendingDeleteBase.value = base; }
async function confirmRemoveBase() { const base = pendingDeleteBase.value; if (!base || deletingBase.value)
    return; deletingBase.value = true; try {
    await api.delete(`/knowledge-bases/${base.id}`);
    pendingDeleteBase.value = undefined;
    deletingBase.value = false;
    if (selected.value?.id === base.id)
        selected.value = undefined;
    await load();
    void appDialog.alert({ title: '删除成功', message: `知识库“${base.name}”已删除。` });
}
catch (error) {
    pendingDeleteBase.value = undefined;
    deletingBase.value = false;
    void appDialog.alert({ title: '删除失败', message: errorText(error, '无法删除该知识库，请检查当前账号权限。') });
} }
onMounted(load);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['create-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['create-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['create-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['create-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['delete-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['delete-modal']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['back']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['summary']} */ ;
/** @type {__VLS_StyleScopedClasses['summary']} */ ;
/** @type {__VLS_StyleScopedClasses['summary']} */ ;
/** @type {__VLS_StyleScopedClasses['summary']} */ ;
/** @type {__VLS_StyleScopedClasses['summary']} */ ;
/** @type {__VLS_StyleScopedClasses['panel-title']} */ ;
/** @type {__VLS_StyleScopedClasses['base-search']} */ ;
/** @type {__VLS_StyleScopedClasses['base-search']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['active']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
/** @type {__VLS_StyleScopedClasses['base-delete']} */ ;
/** @type {__VLS_StyleScopedClasses['empty-bases']} */ ;
/** @type {__VLS_StyleScopedClasses['document-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['document-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['document-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-upload']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-upload']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-upload']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-upload']} */ ;
/** @type {__VLS_StyleScopedClasses['batch-upload']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-head']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-table']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-name']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-name']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-status']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-status']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-status']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-table']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-table']} */ ;
/** @type {__VLS_StyleScopedClasses['empty-docs']} */ ;
/** @type {__VLS_StyleScopedClasses['select-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['empty-docs']} */ ;
/** @type {__VLS_StyleScopedClasses['empty-docs']} */ ;
/** @type {__VLS_StyleScopedClasses['select-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['select-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['select-empty']} */ ;
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
/** @type {__VLS_StyleScopedClasses['summary']} */ ;
/** @type {__VLS_StyleScopedClasses['management-layout']} */ ;
/** @type {__VLS_StyleScopedClasses['base-panel']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-head']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-table']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-table']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-name']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-status']} */ ;
/** @type {__VLS_StyleScopedClasses['doc-status']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.main, __VLS_intrinsics.main)({
    ...{ class: "knowledge-page" },
});
/** @type {__VLS_StyleScopedClasses['knowledge-page']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.header, __VLS_intrinsics.header)({
    ...{ class: "page-header" },
});
/** @type {__VLS_StyleScopedClasses['page-header']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (...[$event]) => {
            return (__VLS_ctx.router.push('/'));
            // @ts-ignore
            [router,];
        } },
    type: "button",
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
    type: "button",
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
    ...{ class: "summary" },
});
/** @type {__VLS_StyleScopedClasses['summary']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
let __VLS_10;
/** @ts-ignore @type { | typeof __VLS_components.BookOpen} */
BookOpen;
// @ts-ignore
const __VLS_11 = __VLS_asFunctionalComponent1(__VLS_10, new __VLS_10({}));
const __VLS_12 = __VLS_11({}, ...__VLS_functionalComponentArgsRest(__VLS_11));
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
(__VLS_ctx.bases.length);
__VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
let __VLS_15;
/** @ts-ignore @type { | typeof __VLS_components.FileText} */
FileText;
// @ts-ignore
const __VLS_16 = __VLS_asFunctionalComponent1(__VLS_15, new __VLS_15({}));
const __VLS_17 = __VLS_16({}, ...__VLS_functionalComponentArgsRest(__VLS_16));
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
(__VLS_ctx.totalDocs);
__VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
let __VLS_20;
/** @ts-ignore @type { | typeof __VLS_components.CheckCircle2} */
CheckCircle2;
// @ts-ignore
const __VLS_21 = __VLS_asFunctionalComponent1(__VLS_20, new __VLS_20({}));
const __VLS_22 = __VLS_21({}, ...__VLS_functionalComponentArgsRest(__VLS_21));
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
(__VLS_ctx.docs.filter(d => d.status === 'READY').length);
__VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "management-layout" },
});
/** @type {__VLS_StyleScopedClasses['management-layout']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.aside, __VLS_intrinsics.aside)({
    ...{ class: "base-panel" },
});
/** @type {__VLS_StyleScopedClasses['base-panel']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "panel-title" },
});
/** @type {__VLS_StyleScopedClasses['panel-title']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
(__VLS_ctx.filtered.length);
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "base-search" },
});
/** @type {__VLS_StyleScopedClasses['base-search']} */ ;
let __VLS_25;
/** @ts-ignore @type { | typeof __VLS_components.Search} */
Search;
// @ts-ignore
const __VLS_26 = __VLS_asFunctionalComponent1(__VLS_25, new __VLS_25({}));
const __VLS_27 = __VLS_26({}, ...__VLS_functionalComponentArgsRest(__VLS_26));
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    placeholder: "搜索知识库",
});
(__VLS_ctx.query);
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "base-list" },
});
/** @type {__VLS_StyleScopedClasses['base-list']} */ ;
for (const [base] of __VLS_vFor((__VLS_ctx.filtered))) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                return (__VLS_ctx.select(base));
                // @ts-ignore
                [bases, totalDocs, docs, filtered, filtered, query, select,];
            } },
        ...{ onKeydown: (...[$event]) => {
                return (__VLS_ctx.select(base));
                // @ts-ignore
                [select,];
            } },
        key: (base.id),
        ...{ class: ({ active: __VLS_ctx.selected?.id === base.id }) },
        role: "button",
        tabindex: "0",
    });
    /** @type {__VLS_StyleScopedClasses['active']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "base-icon" },
        ...{ style: ({ background: `${base.color}18`, color: base.color }) },
    });
    /** @type {__VLS_StyleScopedClasses['base-icon']} */ ;
    (base.icon || '📚');
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
    (base.name);
    __VLS_asFunctionalElement1(__VLS_intrinsics.small, __VLS_intrinsics.small)({});
    (base.documents);
    (base.role);
    if (base.role === 'OWNER') {
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!(base.role === 'OWNER'))
                        throw 0;
                    return (__VLS_ctx.requestRemoveBase(base));
                    // @ts-ignore
                    [selected, requestRemoveBase,];
                } },
            type: "button",
            ...{ class: "base-delete" },
            title: "删除知识库",
        });
        /** @type {__VLS_StyleScopedClasses['base-delete']} */ ;
        let __VLS_30;
        /** @ts-ignore @type { | typeof __VLS_components.Trash2} */
        Trash2;
        // @ts-ignore
        const __VLS_31 = __VLS_asFunctionalComponent1(__VLS_30, new __VLS_30({}));
        const __VLS_32 = __VLS_31({}, ...__VLS_functionalComponentArgsRest(__VLS_31));
    }
    // @ts-ignore
    [];
}
if (!__VLS_ctx.filtered.length && !__VLS_ctx.query) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(!__VLS_ctx.filtered.length && !__VLS_ctx.query))
                    throw 0;
                return (__VLS_ctx.showCreate = true);
                // @ts-ignore
                [showCreate, filtered, query,];
            } },
        type: "button",
        ...{ class: "empty-bases create-empty" },
    });
    /** @type {__VLS_StyleScopedClasses['empty-bases']} */ ;
    /** @type {__VLS_StyleScopedClasses['create-empty']} */ ;
    let __VLS_35;
    /** @ts-ignore @type { | typeof __VLS_components.FolderPlus} */
    FolderPlus;
    // @ts-ignore
    const __VLS_36 = __VLS_asFunctionalComponent1(__VLS_35, new __VLS_35({}));
    const __VLS_37 = __VLS_36({}, ...__VLS_functionalComponentArgsRest(__VLS_36));
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
}
else if (!__VLS_ctx.filtered.length) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "empty-bases" },
    });
    /** @type {__VLS_StyleScopedClasses['empty-bases']} */ ;
    let __VLS_40;
    /** @ts-ignore @type { | typeof __VLS_components.Search} */
    Search;
    // @ts-ignore
    const __VLS_41 = __VLS_asFunctionalComponent1(__VLS_40, new __VLS_40({}));
    const __VLS_42 = __VLS_41({}, ...__VLS_functionalComponentArgsRest(__VLS_41));
}
__VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
    ...{ class: "document-panel" },
});
/** @type {__VLS_StyleScopedClasses['document-panel']} */ ;
if (__VLS_ctx.selected) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.header, __VLS_intrinsics.header)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "library-mark" },
        ...{ style: ({ background: `${__VLS_ctx.selected.color}18`, color: __VLS_ctx.selected.color }) },
    });
    /** @type {__VLS_StyleScopedClasses['library-mark']} */ ;
    (__VLS_ctx.selected.icon || '📚');
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    (__VLS_ctx.selected.name);
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.selected.description || '暂无描述');
    (__VLS_ctx.docs.length);
    if (__VLS_ctx.selected.role !== 'VIEWER') {
        __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({
            ...{ class: "batch-upload" },
            ...{ class: ({ uploading: __VLS_ctx.uploading }) },
        });
        /** @type {__VLS_StyleScopedClasses['batch-upload']} */ ;
        /** @type {__VLS_StyleScopedClasses['uploading']} */ ;
        let __VLS_45;
        /** @ts-ignore @type { | typeof __VLS_components.UploadCloud} */
        UploadCloud;
        // @ts-ignore
        const __VLS_46 = __VLS_asFunctionalComponent1(__VLS_45, new __VLS_45({}));
        const __VLS_47 = __VLS_46({}, ...__VLS_functionalComponentArgsRest(__VLS_46));
        __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
        (__VLS_ctx.uploading ? `正在处理 ${__VLS_ctx.uploadProgress.done}/${__VLS_ctx.uploadProgress.total}` : '批量上传文档');
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
            ...{ onChange: (__VLS_ctx.uploadMany) },
            type: "file",
            multiple: true,
            accept: ".pdf,.doc,.docx,.ppt,.pptx,.txt,.md",
            disabled: (__VLS_ctx.uploading),
        });
        if (__VLS_ctx.uploading) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.i, __VLS_intrinsics.i)({
                ...{ style: ({ width: `${__VLS_ctx.uploadProgress.total ? __VLS_ctx.uploadProgress.done / __VLS_ctx.uploadProgress.total * 100 : 0}%` }) },
            });
        }
    }
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "doc-table" },
    });
    /** @type {__VLS_StyleScopedClasses['doc-table']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "doc-head" },
    });
    /** @type {__VLS_StyleScopedClasses['doc-head']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    for (const [doc] of __VLS_vFor((__VLS_ctx.docs))) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.article, __VLS_intrinsics.article)({
            key: (doc.id),
        });
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "doc-name" },
        });
        /** @type {__VLS_StyleScopedClasses['doc-name']} */ ;
        let __VLS_50;
        /** @ts-ignore @type { | typeof __VLS_components.FileText} */
        FileText;
        // @ts-ignore
        const __VLS_51 = __VLS_asFunctionalComponent1(__VLS_50, new __VLS_50({}));
        const __VLS_52 = __VLS_51({}, ...__VLS_functionalComponentArgsRest(__VLS_51));
        __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
        (doc.fileName);
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
        ((doc.size / 1024).toFixed(1));
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
        (doc.segmentCount);
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "doc-status" },
            ...{ class: (doc.status.toLowerCase()) },
        });
        /** @type {__VLS_StyleScopedClasses['doc-status']} */ ;
        if (doc.status === 'READY') {
            let __VLS_55;
            /** @ts-ignore @type { | typeof __VLS_components.CheckCircle2} */
            CheckCircle2;
            // @ts-ignore
            const __VLS_56 = __VLS_asFunctionalComponent1(__VLS_55, new __VLS_55({}));
            const __VLS_57 = __VLS_56({}, ...__VLS_functionalComponentArgsRest(__VLS_56));
        }
        else {
            let __VLS_60;
            /** @ts-ignore @type { | typeof __VLS_components.XCircle} */
            XCircle;
            // @ts-ignore
            const __VLS_61 = __VLS_asFunctionalComponent1(__VLS_60, new __VLS_60({}));
            const __VLS_62 = __VLS_61({}, ...__VLS_functionalComponentArgsRest(__VLS_61));
        }
        (doc.status);
        if (__VLS_ctx.selected.role !== 'VIEWER') {
            __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
                ...{ onClick: (...[$event]) => {
                        if (!(__VLS_ctx.selected))
                            throw 0;
                        if (!(__VLS_ctx.selected.role !== 'VIEWER'))
                            throw 0;
                        return (__VLS_ctx.removeDocument(doc));
                        // @ts-ignore
                        [docs, docs, filtered, selected, selected, selected, selected, selected, selected, selected, selected, uploading, uploading, uploading, uploading, uploadProgress, uploadProgress, uploadProgress, uploadProgress, uploadProgress, uploadMany, removeDocument,];
                    } },
                title: "删除文档",
            });
            let __VLS_65;
            /** @ts-ignore @type { | typeof __VLS_components.Trash2} */
            Trash2;
            // @ts-ignore
            const __VLS_66 = __VLS_asFunctionalComponent1(__VLS_65, new __VLS_65({}));
            const __VLS_67 = __VLS_66({}, ...__VLS_functionalComponentArgsRest(__VLS_66));
        }
        // @ts-ignore
        [];
    }
    if (!__VLS_ctx.docs.length) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "empty-docs" },
        });
        /** @type {__VLS_StyleScopedClasses['empty-docs']} */ ;
        let __VLS_70;
        /** @ts-ignore @type { | typeof __VLS_components.FileText} */
        FileText;
        // @ts-ignore
        const __VLS_71 = __VLS_asFunctionalComponent1(__VLS_70, new __VLS_70({}));
        const __VLS_72 = __VLS_71({}, ...__VLS_functionalComponentArgsRest(__VLS_71));
        __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({});
    }
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "select-empty" },
    });
    /** @type {__VLS_StyleScopedClasses['select-empty']} */ ;
    let __VLS_75;
    /** @ts-ignore @type { | typeof __VLS_components.BookOpen} */
    BookOpen;
    // @ts-ignore
    const __VLS_76 = __VLS_asFunctionalComponent1(__VLS_75, new __VLS_75({}));
    const __VLS_77 = __VLS_76({}, ...__VLS_functionalComponentArgsRest(__VLS_76));
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!!(__VLS_ctx.selected))
                    throw 0;
                return (__VLS_ctx.showCreate = true);
                // @ts-ignore
                [showCreate, docs,];
            } },
        ...{ class: "primary" },
    });
    /** @type {__VLS_StyleScopedClasses['primary']} */ ;
    let __VLS_80;
    /** @ts-ignore @type { | typeof __VLS_components.Plus} */
    Plus;
    // @ts-ignore
    const __VLS_81 = __VLS_asFunctionalComponent1(__VLS_80, new __VLS_80({}));
    const __VLS_82 = __VLS_81({}, ...__VLS_functionalComponentArgsRest(__VLS_81));
}
if (__VLS_ctx.showCreate) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.showCreate))
                    throw 0;
                return (__VLS_ctx.showCreate = false);
                // @ts-ignore
                [showCreate, showCreate,];
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
    let __VLS_85;
    /** @ts-ignore @type { | typeof __VLS_components.X} */
    X;
    // @ts-ignore
    const __VLS_86 = __VLS_asFunctionalComponent1(__VLS_85, new __VLS_85({}));
    const __VLS_87 = __VLS_86({}, ...__VLS_functionalComponentArgsRest(__VLS_86));
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "eyebrow" },
    });
    /** @type {__VLS_StyleScopedClasses['eyebrow']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        required: true,
        placeholder: "例如：产品研发资料",
    });
    (__VLS_ctx.form.name);
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.textarea, __VLS_intrinsics.textarea)({
        value: (__VLS_ctx.form.description),
        placeholder: "说明该知识库包含的内容",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.label, __VLS_intrinsics.label)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        type: "color",
    });
    (__VLS_ctx.form.color);
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ class: "primary" },
    });
    /** @type {__VLS_StyleScopedClasses['primary']} */ ;
}
if (__VLS_ctx.pendingDeleteBase) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.pendingDeleteBase))
                    throw 0;
                return (__VLS_ctx.pendingDeleteBase = undefined);
                // @ts-ignore
                [form, form, form, pendingDeleteBase, pendingDeleteBase,];
            } },
        ...{ class: "modal-backdrop" },
    });
    /** @type {__VLS_StyleScopedClasses['modal-backdrop']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
        ...{ class: "delete-modal" },
    });
    /** @type {__VLS_StyleScopedClasses['delete-modal']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: "delete-icon" },
    });
    /** @type {__VLS_StyleScopedClasses['delete-icon']} */ ;
    let __VLS_90;
    /** @ts-ignore @type { | typeof __VLS_components.Trash2} */
    Trash2;
    // @ts-ignore
    const __VLS_91 = __VLS_asFunctionalComponent1(__VLS_90, new __VLS_90({}));
    const __VLS_92 = __VLS_91({}, ...__VLS_functionalComponentArgsRest(__VLS_91));
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.pendingDeleteBase.name);
    __VLS_asFunctionalElement1(__VLS_intrinsics.b, __VLS_intrinsics.b)({});
    (__VLS_ctx.pendingDeleteBase.documents);
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                if (!(__VLS_ctx.pendingDeleteBase))
                    throw 0;
                return (__VLS_ctx.pendingDeleteBase = undefined);
                // @ts-ignore
                [pendingDeleteBase, pendingDeleteBase, pendingDeleteBase,];
            } },
        type: "button",
        ...{ class: "cancel-delete" },
        disabled: (__VLS_ctx.deletingBase),
    });
    /** @type {__VLS_StyleScopedClasses['cancel-delete']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (__VLS_ctx.confirmRemoveBase) },
        type: "button",
        ...{ class: "confirm-delete" },
        disabled: (__VLS_ctx.deletingBase),
    });
    /** @type {__VLS_StyleScopedClasses['confirm-delete']} */ ;
    (__VLS_ctx.deletingBase ? '正在删除…' : '永久删除');
}
// @ts-ignore
[deletingBase, deletingBase, deletingBase, confirmRemoveBase,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
