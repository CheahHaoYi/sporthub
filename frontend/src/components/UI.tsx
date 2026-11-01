import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ShoppingBag, ArrowRight, LoaderCircle } from 'lucide-react';
export function Button({children, ...props}:React.ButtonHTMLAttributes<HTMLButtonElement>){return <button className={`button ${props.className||''}`} {...props}>{children}</button>}
export function Loading(){return <div className="loading"><LoaderCircle className="spin"/> Loading SportHub…</div>}
export function ErrorBox({children}:{children:ReactNode}){return <div className="error-box" role="alert">{children}</div>}
export function Empty({title,action}:{title:string;action?:ReactNode}){return <div className="empty"><ShoppingBag size={24}/><strong>{title}</strong>{action}</div>}
export function SectionHeading({eyebrow,title,children}:{eyebrow?:string;title:string;children?:ReactNode}){return <div className="section-heading">{eyebrow&&<span className="eyebrow">{eyebrow}</span>}<h2>{title}</h2>{children}</div>}
export function PageHero({eyebrow,title,description}:{eyebrow:string;title:string;description?:string}){return <div className="page-hero"><span className="eyebrow">{eyebrow}</span><h1>{title}</h1>{description&&<p>{description}</p>}</div>}
export function ActionLink({to,children}:{to:string;children:ReactNode}){return <Link to={to} className="button button-dark">{children}<ArrowRight size={16}/></Link>}
