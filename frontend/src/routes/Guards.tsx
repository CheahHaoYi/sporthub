import { Navigate, Outlet, useLocation } from 'react-router-dom';import { useAuth } from '../contexts/AuthContext';import { Loading } from '../components/UI';
export function CustomerGuard(){const {user,ready}=useAuth();const loc=useLocation();if(!ready)return <Loading/>;return user?.role==='CUSTOMER'?<Outlet/>:<Navigate to="/login" state={{from:loc.pathname}} replace/>}
export function AdminGuard(){const {user,ready}=useAuth();if(!ready)return <Loading/>;return user?.role==='ADMIN'?<Outlet/>:<Navigate to="/login" replace/>}
