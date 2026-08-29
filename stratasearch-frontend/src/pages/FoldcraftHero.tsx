import { useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, Menu, X } from "lucide-react";

export default function FoldcraftHero() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <div className="relative h-screen w-full overflow-hidden bg-black font-geist -webkit-font-smoothing-antialiased -moz-osx-font-smoothing-grayscale">
      {/* Video Background */}
      <video
        autoPlay
        muted
        loop
        playsInline
        className="absolute inset-0 h-full w-full object-cover object-[70%_center]"
      >
        <source
          src="https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260622_204221_5339e40b-e73d-4ab0-9c65-79c18c66fd50.mp4"
          type="video/mp4"
        />
      </video>

      {/* Dark overlay */}
      <div className="absolute inset-0 bg-black/60 pointer-events-none z-0" />

      {/* Navbar (z-30) */}
      <header className="sticky top-0 z-30 flex items-center justify-between px-6 py-4 md:px-12 lg:px-16 border-b border-white/[0.06] bg-black/35 backdrop-blur-md">
        {/* Left side: Logo & Desktop Links */}
        <div className="flex items-center gap-8">
          <Link to="/" className="text-lg font-semibold tracking-tight text-white sm:text-xl">
            Strata<span className="text-amber">Search</span>
          </Link>
          <nav className="hidden items-center gap-6 md:flex">
            <Link to="/" className="text-sm text-white/80 transition-colors hover:text-white">
              Home
            </Link>
            <Link to="/lab" className="text-sm text-white/80 transition-colors hover:text-white">
              Strata Lab
            </Link>
            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="text-sm text-white/80 transition-colors hover:text-white"
            >
              API Docs
            </a>
          </nav>
        </div>

        {/* Right side (desktop): Launch Lab CTA */}
        <div className="hidden md:block">
          <Link
            to="/lab"
            className="rounded-lg bg-white px-5 py-2 text-sm font-semibold text-zinc-950 transition-all hover:scale-105 inline-block shadow-md"
          >
            Launch Lab
          </Link>
        </div>

        {/* Right side (mobile): Hamburger toggle button */}
        <button
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          className="relative z-50 flex h-10 w-10 items-center justify-center text-white active:scale-90 md:hidden"
          aria-label="Toggle menu"
        >
          <div className="relative h-6 w-6">
            <Menu
              className={`absolute inset-0 h-6 w-6 transition-all duration-300 ${
                mobileMenuOpen ? "rotate-90 scale-0 opacity-0" : "rotate-0 scale-100 opacity-100"
              }`}
            />
            <X
              className={`absolute inset-0 h-6 w-6 transition-all duration-300 ${
                mobileMenuOpen ? "rotate-0 scale-100 opacity-100" : "-rotate-90 scale-0 opacity-0"
              }`}
            />
          </div>
        </button>
      </header>

      {/* Mobile Menu (z-20) */}
      <div
        className={`absolute inset-x-0 top-0 z-20 bg-black/98 backdrop-blur-xl transition-all duration-500 ease-[cubic-bezier(0.16,1,0.3,1)] ${
          mobileMenuOpen ? "h-screen opacity-100" : "pointer-events-none h-0 opacity-0"
        }`}
      >
        <div
          className={`flex h-full flex-col justify-center px-8 transition-all duration-500 delay-100 ${
            mobileMenuOpen ? "translate-y-0 opacity-100" : "translate-y-8 opacity-0"
          }`}
        >
          <nav className="flex flex-col gap-6">
            <Link
              to="/"
              onClick={() => setMobileMenuOpen(false)}
              className="text-3xl font-medium text-white/90 hover:text-white"
            >
              Home
            </Link>
            <Link
              to="/lab"
              onClick={() => setMobileMenuOpen(false)}
              className="text-3xl font-medium text-white/90 hover:text-white"
            >
              Strata Lab
            </Link>
            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              onClick={() => setMobileMenuOpen(false)}
              className="text-3xl font-medium text-white/90 hover:text-white"
            >
              API Docs
            </a>
          </nav>
          <Link
            to="/lab"
            onClick={() => setMobileMenuOpen(false)}
            className="mt-6 self-start rounded-full bg-white px-8 py-3.5 text-base font-medium text-black hover:scale-105 inline-block"
          >
            Launch Lab
          </Link>
        </div>
      </div>

      {/* Hero Content (z-10) */}
      <main className="relative z-10 flex h-[calc(100vh-80px)] flex-col justify-between px-6 pt-12 pb-10 sm:pt-16 sm:pb-12 md:px-12 md:pt-20 md:pb-16 lg:px-16">
        {/* Top Section */}
        <div className="max-w-3xl">
          <div className="flex items-center gap-2 mb-4 sm:mb-6 animate-[fadeSlideUp_0.8s_ease_0.2s_both]">
            <span className="h-1.5 w-1.5 rounded-full bg-blue-500 animate-pulse" />
            <p className="text-xs font-mono tracking-widest text-blue-400 uppercase">
              High-Performance String Matching
            </p>
          </div>
          <h1 className="text-4xl font-semibold leading-[1.08] tracking-tight text-white sm:text-5xl md:text-6xl lg:text-7xl animate-[fadeSlideUp_0.8s_ease_0.4s_both] bg-gradient-to-br from-white via-white to-blue-200 bg-clip-text text-transparent">
            Engineered for speed, <br /> designed for <br /> algorithms.
          </h1>
        </div>

        {/* Bottom Section */}
        <div>
          <p className="mb-5 max-w-sm text-sm leading-relaxed text-white/60 sm:mb-6 sm:max-w-lg sm:text-base md:text-lg animate-[fadeSlideUp_0.8s_ease_0.7s_both]">
            Evaluate classical string matching algorithms head-to-head, analyze corpus statistics, and explore step-by-step executions in a premium console.
          </p>
          <Link
            to="/lab"
            className="inline-flex items-center gap-2 rounded-lg bg-white/95 backdrop-blur-md px-5 py-2.5 text-sm font-semibold text-zinc-950 transition-all hover:bg-white hover:scale-105 sm:px-6 sm:py-3 animate-[fadeSlideUp_0.8s_ease_0.9s_both] shadow-lg"
          >
            Launch Strata Lab
            <ArrowRight size={16} />
          </Link>
        </div>
      </main>
    </div>
  );
}
