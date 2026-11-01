export const money=(value?:string|number)=>`SGD ${Number(value||0).toFixed(2)}`;
export function unwrap<T>(data:unknown):T[]{if(Array.isArray(data))return data as T[];if(data&&typeof data==='object'){const d=data as Record<string,unknown>;for(const k of ['content','items','data','results'])if(Array.isArray(d[k]))return d[k] as T[]}return []}
export function idOf(x:Record<string,unknown>){return String(x.id??x.uuid??'')}
