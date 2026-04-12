import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  ShoppingCart, Heart, LogOut, LogIn, Menu, X, BarChart3,
  Package, User, Zap, ClipboardList, Tag, LayoutDashboard, Shield, Store
} from 'lucide-react';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileOpen, setMobileOpen] = useState(false);

  const isAdmin = (() => {
    try {
      const stored = localStorage.getItem('user');
      if (stored) {
        const u = JSON.parse(stored);
        return u.role === 'ROLE_ADMIN';
      }
      return false;
    } catch { return false; }
  })();

  const isAdminPage = location.pathname.startsWith('/admin') || location.pathname === '/ai-dashboard';

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  const NavLink = ({ to, icon: Icon, label }) => (
    <Link
      to={to}
      onClick={() => setMobileOpen(false)}
      className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200
        ${isActive(to)
          ? 'bg-emerald-500/10 text-emerald-400'
          : 'text-zinc-400 hover:text-white hover:bg-white/5'
        }`}
    >
      <Icon size={16} />
      <span>{label}</span>
    </Link>
  );

  // ─── ADMIN NAVBAR ───
  if (isAdmin && isAdminPage) {
    return (
      <nav className="sticky top-0 z-50 backdrop-blur-xl bg-zinc-950/80 border-b border-amber-500/10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6">
          <div className="flex items-center justify-between h-16">
            <Link to="/admin" className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-amber-400 to-orange-500 flex items-center justify-center">
                <Shield size={16} className="text-zinc-900" />
              </div>
              <span className="text-lg font-bold text-white tracking-tight">
                Shop<span className="text-amber-400">AI</span>
                <span className="text-xs ml-2 bg-amber-500/20 text-amber-400 px-2 py-0.5 rounded-full font-bold">ADMIN</span>
              </span>
            </Link>

            <div className="hidden md:flex items-center gap-1">
              <NavLink to="/admin" icon={LayoutDashboard} label="Dashboard" />
              <NavLink to="/admin/products" icon={Package} label="Products" />
              <NavLink to="/admin/orders" icon={ClipboardList} label="Orders" />
              <NavLink to="/admin/coupons" icon={Tag} label="Coupons" />
              <NavLink to="/ai-dashboard" icon={BarChart3} label="AI Pricing" />
              <Link to="/"
                className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-emerald-400 hover:bg-emerald-500/10 ml-2 border border-emerald-500/20">
                <Store size={14} /> View Store
              </Link>
            </div>

            <div className="hidden md:flex items-center gap-3">
              <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-amber-500/10 border border-amber-500/20">
                <User size={14} className="text-amber-400" />
                <span className="text-sm text-zinc-300">{user?.username}</span>
              </div>
              <button onClick={handleLogout} className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm text-zinc-400 hover:text-red-400 hover:bg-red-500/10">
                <LogOut size={14} /> Logout
              </button>
            </div>

            <button onClick={() => setMobileOpen(!mobileOpen)} className="md:hidden text-zinc-400 hover:text-white">
              {mobileOpen ? <X size={22} /> : <Menu size={22} />}
            </button>
          </div>

          {mobileOpen && (
            <div className="md:hidden pb-4 space-y-1 border-t border-white/5 pt-3">
              <NavLink to="/admin" icon={LayoutDashboard} label="Dashboard" />
              <NavLink to="/admin/products" icon={Package} label="Products" />
              <NavLink to="/admin/orders" icon={ClipboardList} label="Orders" />
              <NavLink to="/admin/coupons" icon={Tag} label="Coupons" />
              <NavLink to="/ai-dashboard" icon={BarChart3} label="AI Pricing" />
              <Link to="/" onClick={() => setMobileOpen(false)}
                className="flex items-center gap-2 px-3 py-2 text-sm text-emerald-400">
                <Store size={16} /> View Store
              </Link>
              <button onClick={handleLogout} className="flex items-center gap-2 px-3 py-2 text-sm text-red-400 w-full">
                <LogOut size={16} /> Logout
              </button>
            </div>
          )}
        </div>
      </nav>
    );
  }

  // ─── CUSTOMER NAVBAR ───
  return (
    <nav className="sticky top-0 z-50 backdrop-blur-xl bg-zinc-950/80 border-b border-white/5">
      <div className="max-w-7xl mx-auto px-4 sm:px-6">
        <div className="flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-400 to-cyan-400 flex items-center justify-center">
              <Zap size={16} className="text-zinc-900" />
            </div>
            <span className="text-lg font-bold text-white tracking-tight">
              Shop<span className="text-emerald-400">AI</span>
            </span>
          </Link>

          <div className="hidden md:flex items-center gap-1">
            <NavLink to="/" icon={Package} label="Products" />
            {user && (
              <>
                <NavLink to="/cart" icon={ShoppingCart} label="Cart" />
                <NavLink to="/wishlist" icon={Heart} label="Wishlist" />
                <NavLink to="/orders" icon={ClipboardList} label="Orders" />
                {isAdmin && (
                  <Link to="/admin"
                    className="flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium text-amber-400 hover:bg-amber-500/10 ml-2 border border-amber-500/20">
                    <Shield size={14} /> Admin Panel
                  </Link>
                )}
              </>
            )}
          </div>

          <div className="hidden md:flex items-center gap-3">
            {user ? (
              <>
                <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-white/5 border border-white/10">
                  <User size={14} className="text-emerald-400" />
                  <span className="text-sm text-zinc-300">{user.username}</span>
                </div>
                <button onClick={handleLogout} className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm text-zinc-400 hover:text-red-400 hover:bg-red-500/10">
                  <LogOut size={14} /> Logout
                </button>
              </>
            ) : (
              <Link to="/login" className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-emerald-500 text-zinc-900 text-sm font-semibold hover:bg-emerald-400">
                <LogIn size={14} /> Sign In
              </Link>
            )}
          </div>

          <button onClick={() => setMobileOpen(!mobileOpen)} className="md:hidden text-zinc-400 hover:text-white">
            {mobileOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </div>

        {mobileOpen && (
          <div className="md:hidden pb-4 space-y-1 border-t border-white/5 pt-3">
            <NavLink to="/" icon={Package} label="Products" />
            {user && (
              <>
                <NavLink to="/cart" icon={ShoppingCart} label="Cart" />
                <NavLink to="/wishlist" icon={Heart} label="Wishlist" />
                <NavLink to="/orders" icon={ClipboardList} label="Orders" />
                {isAdmin && (
                  <NavLink to="/admin" icon={Shield} label="Admin Panel" />
                )}
              </>
            )}
            {user ? (
              <button onClick={handleLogout} className="flex items-center gap-2 px-3 py-2 text-sm text-red-400 w-full">
                <LogOut size={16} /> Logout
              </button>
            ) : (
              <NavLink to="/login" icon={LogIn} label="Sign In" />
            )}
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;