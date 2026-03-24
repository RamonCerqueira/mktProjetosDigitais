import Link from "next/link";

export function Footer() {
  return (
    <footer className="border-t border-slate-800 bg-slate-950/90 mt-16">
      <div className="mx-auto flex max-w-6xl flex-col gap-3 px-6 py-6 text-sm text-slate-400 md:flex-row md:items-center md:justify-between">
        <p>© MicroSaaS Market — operação, observabilidade e compliance para marketplace digital.</p>
        <nav className="flex gap-4">
          <Link href="/terms">Termos de uso</Link>
          <Link href="/privacy">Privacidade</Link>
          <Link href="/marketplace-rules">Regras do marketplace</Link>
        </nav>
      </div>
    </footer>
  );
}
