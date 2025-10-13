import "./../styles/globals.css";
import type { ReactNode } from "react";
import Providers from "./providers";
import Header from "@/components/Header";

export const metadata = {
  title: "Next × Spring Boilerplate",
  description: "Frontend for a Spring backend",
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <Providers>
          <Header />
          <main className="container">{children}</main>
        </Providers>
      </body>
    </html>
  );
}
