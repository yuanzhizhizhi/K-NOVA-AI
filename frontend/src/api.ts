import axios from 'axios';
export const api=axios.create({baseURL:'/api'}); api.interceptors.request.use(c=>{const t=localStorage.getItem('token');if(t)c.headers.Authorization=`Bearer ${t}`;return c}); api.interceptors.response.use(r=>r,e=>{if(e.response?.status===401){localStorage.clear();location.href='/login'}return Promise.reject(e)});
