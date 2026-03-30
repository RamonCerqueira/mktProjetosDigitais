import Link from "next/link";

export function Footer() {
  return (
    <footer className="mt-16 border-t border-slate-200 bg-white/80">
      <div className="mx-auto flex max-w-7xl flex-col gap-3 px-4 py-8 text-sm text-slate-500 lg:px-8 md:flex-row md:items-center md:justify-between">
        <p>© 2026 Portal de Projetos Digitais — marketplace vivo para compra e venda de produtos digitais.</p>
        <nav className="flex gap-4">
          <Link href="/terms">Termos</Link>
          <Link href="/privacy">Privacidade</Link>
          <Link href="/marketplace-rules">Regras</Link>
        </nav>
      </div>
    </footer>
  );
}
