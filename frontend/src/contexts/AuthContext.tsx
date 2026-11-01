import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../api';
import type { AuthResponse, User } from '../types';
interface AuthValue { user: User|null; ready:boolean; login:(email:string,password:string)=>Promise<void>; register:(body:{email:string;password:string;fullName:string;dateOfBirth:string})=>Promise<void>; logout:()=>void; }
const Context=createContext<AuthValue|null>(null);
export function AuthProvider({children}:{children:React.ReactNode}) { const [user,setUser]=useState<User|null>(()=>{try{return JSON.parse(localStorage.getItem('sporthub_user')||'null')}catch{return null}}); const [ready,setReady]=useState(false);
 useEffect(()=>{ const clear=()=>{setUser(null);setReady(true)}; window.addEventListener('sporthub:unauthorized',clear); const token=localStorage.getItem('sporthub_token'); if(token) authApi.me().then(u=>{setUser(u);localStorage.setItem('sporthub_user',JSON.stringify(u))}).catch(()=>{localStorage.removeItem('sporthub_token');localStorage.removeItem('sporthub_user');setUser(null)}).finally(()=>setReady(true)); else setReady(true); return()=>window.removeEventListener('sporthub:unauthorized',clear); },[]);
 const login=async(email:string,password:string)=>{const result:AuthResponse=await authApi.login({email,password});localStorage.setItem('sporthub_token',result.accessToken);localStorage.setItem('sporthub_user',JSON.stringify(result.user));setUser(result.user)};
 const register=async(body:{email:string;password:string;fullName:string;dateOfBirth:string})=>{await authApi.register(body);await login(body.email,body.password)};
 const logout=()=>{localStorage.removeItem('sporthub_token');localStorage.removeItem('sporthub_user');setUser(null)};
 const value=useMemo(()=>({user,ready,login,register,logout}),[user,ready]);return <Context.Provider value={value}>{children}</Context.Provider> }
export function useAuth(){const value=useContext(Context);if(!value)throw new Error('useAuth must be used within AuthProvider');return value}
