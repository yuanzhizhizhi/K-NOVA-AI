import {reactive} from 'vue';

type DialogKind='alert'|'confirm'|'prompt';
type DialogOptions={title?:string,message:string,confirmText?:string,cancelText?:string,danger?:boolean,inputType?:'text'|'password',placeholder?:string};
type Resolver=(value:boolean|string|null)=>void;

const state=reactive({visible:false,kind:'alert' as DialogKind,title:'',message:'',confirmText:'确定',cancelText:'取消',danger:false,inputType:'text' as 'text'|'password',placeholder:'',value:''});
let resolver:Resolver|undefined;

function open(kind:DialogKind,options:DialogOptions){return new Promise<boolean|string|null>(resolve=>{resolver=resolve;Object.assign(state,{visible:true,kind,title:options.title||({alert:'提示',confirm:'请确认',prompt:'请输入'} as const)[kind],message:options.message,confirmText:options.confirmText||'确定',cancelText:options.cancelText||'取消',danger:!!options.danger,inputType:options.inputType||'text',placeholder:options.placeholder||'',value:''})})}
function finish(confirmed:boolean){const result=confirmed?(state.kind==='prompt'?state.value:true):(state.kind==='alert'?true:null);state.visible=false;resolver?.(result);resolver=undefined}

export const appDialog={state,finish,alert:(options:DialogOptions)=>open('alert',options) as Promise<boolean>,confirm:(options:DialogOptions)=>open('confirm',options) as Promise<boolean|null>,prompt:(options:DialogOptions)=>open('prompt',options) as Promise<string|null>};
