<script setup lang="ts">
import {computed,onMounted,ref} from 'vue';
import {api} from '../api';
import Composer from '../components/Composer.vue';
import {BookOpen,Plus,Search,FileText,Trash2,UploadCloud,Database,LogOut,X,MessageSquare} from 'lucide-vue-next';
type KB={id:number,name:string,description:string,color:string,documents:number};
type Doc={id:number,fileName:string,size:number,status:string,segmentCount:number};
type Msg={id?:number,role:'user'|'assistant',text:string};
type Conversation={id:number,title:string,knowledgeBaseId:number,updatedAt:string};
const bases=ref<KB[]>([]),selected=ref<KB>(),docs=ref<Doc[]>([]),messages=ref<Msg[]>([]),conversations=ref<Conversation[]>([]);
const activeConversationId=ref<number>(),busy=ref(false),showCreate=ref(false),showDocs=ref(false),query=ref('');
const newBase=ref({name:'',description:'',color:'#6D5EF5'}),displayName=localStorage.getItem('name')||'知识库管理员';
const filtered=computed(()=>bases.value.filter(x=>x.name.toLowerCase().includes(query.value.toLowerCase())));

async function load(){bases.value=(await api.get('/knowledge-bases')).data;if(!selected.value&&bases.value[0])await selectBase(bases.value[0])}
async function selectBase(k:KB){selected.value=k;newChat();const [d,c]=await Promise.all([api.get(`/knowledge-bases/${k.id}/documents`),api.get(`/knowledge-bases/${k.id}/conversations`)]);docs.value=d.data;conversations.value=c.data}
function newChat(){activeConversationId.value=undefined;messages.value=[]}
async function openConversation(c:Conversation){activeConversationId.value=c.id;const{data}=await api.get(`/conversations/${c.id}/messages`);messages.value=data.map((m:any)=>({id:m.id,role:m.role,text:m.content}))}
async function deleteConversation(c:Conversation,e:Event){e.stopPropagation();if(!confirm(`确认删除对话“${c.title}”吗？`))return;await api.delete(`/conversations/${c.id}`);conversations.value=conversations.value.filter(x=>x.id!==c.id);if(activeConversationId.value===c.id)newChat()}
async function create(){const{data}=await api.post('/knowledge-bases',newBase.value);showCreate.value=false;newBase.value={name:'',description:'',color:'#6D5EF5'};await load();await selectBase(data)}
async function upload(e:Event){const input=e.target as HTMLInputElement,file=input.files?.[0];if(!file||!selected.value)return;const form=new FormData();form.append('file',file);busy.value=true;try{await api.post(`/knowledge-bases/${selected.value.id}/documents`,form);await selectBase(selected.value);await load()}finally{busy.value=false;input.value=''}}
async function removeDoc(d:Doc){if(!confirm(`删除“${d.fileName}”及其全部向量？`))return;await api.delete(`/documents/${d.id}`);await selectBase(selected.value!);await load()}
async function ask(text:string){if(!selected.value)return;messages.value.push({role:'user',text});busy.value=true;try{const{data}=await api.post(`/knowledge-bases/${selected.value.id}/chat`,{conversationId:activeConversationId.value,question:text});activeConversationId.value=data.conversationId;messages.value.push({role:'assistant',text:data.answer});conversations.value=(await api.get(`/knowledge-bases/${selected.value.id}/conversations`)).data}catch{messages.value.push({role:'assistant',text:'暂时无法连接 AI 服务，请检查模型和向量库配置。'})}finally{busy.value=false}}
function logout(){localStorage.clear();location.href='/login'}
onMounted(load);
</script>

<template><main class="app-shell">
  <aside>
    <div class="brand"><span class="brand-mark"><BookOpen :size="20"/></span>K·NOVA</div>
    <button class="new-chat" @click="newChat"><Plus/>新对话</button>
    <nav>
      <div class="nav-title"><span>知识库</span><button @click="showCreate=true"><Plus/></button></div>
      <div class="search"><Search/><input v-model="query" placeholder="搜索知识库"></div>
      <button v-for="k in filtered" :key="k.id" class="kb-item" :class="{active:selected?.id===k.id}" @click="selectBase(k)"><span class="kb-dot" :style="{background:k.color}"></span><span><b>{{k.name}}</b><small>{{k.documents}} 个文档</small></span></button>
      <div v-if="selected" class="nav-title conversation-title"><span>历史对话</span><small>{{conversations.length}}</small></div>
      <button v-for="c in conversations" :key="c.id" class="conversation-item" :class="{active:activeConversationId===c.id}" @click="openConversation(c)"><MessageSquare/><span>{{c.title}}</span><button title="删除对话" @click="deleteConversation(c,$event)"><Trash2/></button></button>
      <p v-if="selected&&!conversations.length" class="conversation-empty">暂无历史对话</p>
    </nav>
    <div class="profile"><span class="avatar">知</span><span><b>{{displayName}}</b><small>管理员</small></span><button @click="logout"><LogOut/></button></div>
  </aside>
  <section class="workspace">
    <header><div><span class="crumb">知识库 / </span><b>{{selected?.name||'请选择知识库'}}</b></div><button class="secondary" :disabled="!selected" @click="showDocs=true"><Database/>管理知识</button></header>
    <div v-if="!selected" class="empty"><div class="empty-icon"><BookOpen/></div><h2>创建第一个知识库</h2><p>上传内部文档，开启团队 AI 问答空间。</p><button class="primary" @click="showCreate=true"><Plus/>创建知识库</button></div>
    <div v-else class="chat">
      <div v-if="!messages.length" class="welcome"><div class="spark">✦</div><span class="eyebrow">{{selected.name}}</span><h1>今天想了解什么？</h1><p>{{selected.description||'我会基于知识库内容回答，并标注信息来源。'}}</p><div class="suggestions"><button @click="ask('请概括这个知识库包含的主要内容')">概括知识库主要内容<span>↗</span></button><button @click="ask('有哪些关键流程或注意事项？')">关键流程与注意事项<span>↗</span></button><button @click="showDocs=true">查看已收录文档<span>↗</span></button></div></div>
      <div v-else class="messages"><article v-for="(m,i) in messages" :key="m.id||i" :class="m.role"><div class="msg-avatar">{{m.role==='user'?'我':'✦'}}</div><div><b>{{m.role==='user'?'你':'K·NOVA'}}</b><p>{{m.text}}</p></div></article><article v-if="busy" class="assistant"><div class="msg-avatar">✦</div><div><b>K·NOVA</b><p class="thinking">正在检索知识…</p></div></article></div>
      <div class="composer-wrap"><Composer :disabled="busy" @send="ask"/><small>AI 可能会犯错，请核对重要信息与引用来源。</small></div>
    </div>
  </section>
  <div v-if="showCreate" class="modal-backdrop" @click.self="showCreate=false"><form class="modal" @submit.prevent="create"><button type="button" class="modal-close" @click="showCreate=false"><X/></button><span class="eyebrow">NEW SPACE</span><h2>创建知识库</h2><label>名称<input v-model="newBase.name" required placeholder="例如：产品研发手册"></label><label>描述<textarea v-model="newBase.description" placeholder="这个知识库主要用于…"></textarea></label><div class="colors"><button v-for="c in ['#6D5EF5','#00A884','#E68A3F','#DE5B79','#3478F6']" :key="c" type="button" :style="{background:c}" :class="{chosen:newBase.color===c}" @click="newBase.color=c"></button></div><button class="primary" :disabled="!newBase.name">创建知识库</button></form></div>
  <div v-if="showDocs" class="drawer-backdrop" @click.self="showDocs=false"><section class="drawer"><header><div><span class="eyebrow">KNOWLEDGE SOURCES</span><h2>知识库文档</h2><p>{{selected?.name}} · {{docs.length}} 个文档</p></div><button class="modal-close" @click="showDocs=false"><X/></button></header><label class="upload" :class="{loading:busy}"><UploadCloud/><b>{{busy?'正在解析并向量化…':'上传文档'}}</b><span>支持 PDF、Word、PPT、TXT、Markdown，单文件不超过 30MB</span><input type="file" accept=".pdf,.doc,.docx,.ppt,.pptx,.txt,.md" @change="upload" :disabled="busy"></label><div class="doc-list"><article v-for="d in docs" :key="d.id"><span class="file-icon"><FileText/></span><div><b>{{d.fileName}}</b><small>{{(d.size/1024).toFixed(1)}} KB · {{d.segmentCount}} 个片段</small></div><span class="status" :class="d.status.toLowerCase()">{{d.status}}</span><button @click="removeDoc(d)"><Trash2/></button></article><div v-if="!docs.length" class="no-docs">还没有文档，上传一份开始构建知识库。</div></div></section></div>
</main></template>
