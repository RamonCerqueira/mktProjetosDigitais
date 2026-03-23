import "./globals.css";
import { Header } from "@/components/Header";

export const metadata = { title: "MicroSaaS Market", description: "Marketplace de projetos digitais monetizado por assinatura" };

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="pt-BR">
      <body>
        <Header />
        {children}
      </body>
    </html>
  );
}
