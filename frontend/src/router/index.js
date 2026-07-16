import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Workspace from '../views/Workspace.vue';
const router = createRouter({ history: createWebHistory(), routes: [{ path: '/login', component: Login }, { path: '/', component: Workspace }] });
router.beforeEach(to => !localStorage.getItem('token') && to.path != '/login' ? '/login' : true);
export default router;
