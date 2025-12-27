import React, { useState, useEffect } from 'react';
import {
    Search,
    Download,
    Github,
    Star,
    Info,
    Code,
    Layout,
    Cpu,
    Globe,
    Settings,
    Menu,
    X,
    ArrowRight,
    ExternalLink,
    ChevronRight,
    ShieldCheck,
    Zap
} from 'lucide-react';

// Mock Data für die GitHub Apps
const APPS_DATA = [
    {
        id: 1,
        name: "OctoEdit",
        developer: "GitHub Community",
        stars: "12.4k",
        description: "Ein leichtgewichtiger Markdown-Editor mit nativer GitHub-Synchronisation.",
        category: "Developer Tools",
        version: "2.4.1",
        tags: ["Markdown", "Open Source"],
        isFeatured: true
    },
    {
        id: 2,
        name: "GitPulse",
        developer: "MetricsLabs",
        stars: "8.9k",
        description: "Echtzeit-Dashboard für Repository-Metriken und Team-Produktivität.",
        category: "Productivity",
        version: "1.0.5",
        tags: ["Analytics", "Dashboards"],
        isFeatured: false
    },
    {
        id: 3,
        name: "PromptFlow",
        developer: "AI Research Group",
        stars: "15.2k",
        description: "Managte deine LLM-Prompts direkt in deinem Git-Workflow.",
        category: "AI",
        version: "0.9.8",
        tags: ["AI", "Prompt Engineering"],
        isFeatured: true
    },
    {
        id: 4,
        name: "CommitGraph",
        developer: "VisualGit",
        stars: "5.1k",
        description: "Visualisiere komplexe Branch-Strukturen mit interaktiven Graphen.",
        category: "Developer Tools",
        version: "3.2.0",
        tags: ["Visualizer", "Git"],
        isFeatured: false
    },
    {
        id: 5,
        name: "DeployBeacon",
        developer: "CloudOps",
        stars: "7.3k",
        description: "Automatisierte Deployment-Benachrichtigungen für GitHub Actions.",
        category: "Utilities",
        version: "2.1.1",
        tags: ["CI/CD", "DevOps"],
        isFeatured: false
    },
    {
        id: 6,
        name: "SecureAudit",
        developer: "GuardRail",
        stars: "10.8k",
        description: "Scannt Repositories nach Sicherheitslücken und Secrets in Echtzeit.",
        category: "Security",
        version: "1.5.0",
        tags: ["Security", "Audit"],
        isFeatured: false
    }
];

const CATEGORIES = ["Alle", "AI", "Developer Tools", "Productivity", "Utilities", "Security"];

const App = () => {
    const [activeTab, setActiveTab] = useState("Alle");
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedApp, setSelectedApp] = useState(null);
    const [isSidebarOpen, setIsSidebarOpen] = useState(true);

    // Filter Logik
    const filteredApps = APPS_DATA.filter(app => {
        const matchesCategory = activeTab === "Alle" || app.category === activeTab;
        const matchesSearch = app.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            app.description.toLowerCase().includes(searchQuery.toLowerCase());
        return matchesCategory && matchesSearch;
    });

    const featuredApps = APPS_DATA.filter(app => app.isFeatured);

    return (
        <div className="flex h-screen bg-[#111111] text-[#f0f0f0] font-sans overflow-hidden selection:bg-[#d9775733] selection:text-[#d97757]">
            {/* Sidebar - Claude Style Minimalism */}
            <aside className={`${isSidebarOpen ? 'w-64' : 'w-0'} transition-all duration-300 border-r border-[#2a2a2a] bg-[#0d0d0d] flex flex-col`}>
                <div className="p-6 flex items-center gap-3">
                    <div className="w-8 h-8 bg-[#d97757] rounded flex items-center justify-center">
                        <Github size={20} className="text-black" />
                    </div>
                    <span className="font-medium text-lg tracking-tight">GitStore</span>
                </div>

                <nav className="flex-1 px-4 space-y-1">
                    <div className="text-[11px] font-semibold text-[#666] uppercase tracking-wider mb-2 px-2 mt-4">Kategorien</div>
                    {CATEGORIES.map(cat => (
                        <button
                            key={cat}
                            onClick={() => setActiveTab(cat)}
                            className={`w-full text-left px-3 py-2 rounded-md text-sm transition-colors flex items-center gap-3 ${activeTab === cat ? 'bg-[#222] text-[#d97757]' : 'text-[#999] hover:bg-[#1a1a1a] hover:text-[#ccc]'
                                }`}
                        >
                            {cat === "Alle" && <Layout size={16} />}
                            {cat === "AI" && <Cpu size={16} />}
                            {cat === "Developer Tools" && <Code size={16} />}
                            {cat === "Productivity" && <Zap size={16} />}
                            {cat === "Utilities" && <Info size={16} />}
                            {cat === "Security" && <ShieldCheck size={16} />}
                            {cat}
                        </button>
                    ))}

                    <div className="text-[11px] font-semibold text-[#666] uppercase tracking-wider mb-2 px-2 mt-8">Mein Account</div>
                    <button className="w-full text-left px-3 py-2 rounded-md text-sm text-[#999] hover:bg-[#1a1a1a] flex items-center gap-3">
                        <Download size={16} /> Installiert
                    </button>
                    <button className="w-full text-left px-3 py-2 rounded-md text-sm text-[#999] hover:bg-[#1a1a1a] flex items-center gap-3">
                        <Settings size={16} /> Einstellungen
                    </button>
                </nav>

                <div className="p-4 border-t border-[#2a2a2a]">
                    <div className="flex items-center gap-3 p-2 rounded-lg hover:bg-[#1a1a1a] cursor-pointer transition-colors">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-[#333] to-[#555]" />
                        <div className="flex-1 overflow-hidden">
                            <div className="text-sm font-medium truncate">DevUser_2024</div>
                            <div className="text-xs text-[#666] truncate">Pro Plan</div>
                        </div>
                    </div>
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="flex-1 flex flex-col relative overflow-hidden">
                {/* Header */}
                <header className="h-16 border-b border-[#2a2a2a] flex items-center justify-between px-8 bg-[#111111]/80 backdrop-blur-md z-10 sticky top-0">
                    <div className="flex items-center gap-4 flex-1 max-w-2xl">
                        <div className="relative w-full">
                            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-[#666]" size={18} />
                            <input
                                type="text"
                                placeholder="Apps, Entwickler oder Tools suchen..."
                                className="w-full bg-[#1a1a1a] border border-[#2a2a2a] rounded-full py-2 pl-10 pr-4 text-sm focus:outline-none focus:border-[#d97757] transition-all placeholder:text-[#444]"
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                            />
                        </div>
                    </div>
                    <div className="flex items-center gap-4 ml-4">
                        <button className="p-2 text-[#999] hover:text-[#fff] transition-colors">
                            <Globe size={20} />
                        </button>
                    </div>
                </header>

                {/* Scrollable Content */}
                <div className="flex-1 overflow-y-auto p-8 custom-scrollbar">
                    {activeTab === "Alle" && !searchQuery && (
                        <section className="mb-12">
                            <div className="flex items-center justify-between mb-6">
                                <h2 className="text-2xl font-light tracking-tight text-white">Featured Apps</h2>
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                {featuredApps.map(app => (
                                    <div
                                        key={app.id}
                                        onClick={() => setSelectedApp(app)}
                                        className="group bg-[#161616] border border-[#2a2a2a] rounded-xl p-6 cursor-pointer hover:border-[#444] transition-all relative overflow-hidden"
                                    >
                                        <div className="relative z-10 flex flex-col h-full justify-between">
                                            <div>
                                                <div className="flex items-center justify-between mb-4">
                                                    <span className="text-[10px] uppercase tracking-widest text-[#d97757] font-bold bg-[#d977571a] px-2 py-0.5 rounded">Featured</span>
                                                    <span className="flex items-center gap-1 text-xs text-[#666]"><Star size={12} className="fill-[#666]" /> {app.stars}</span>
                                                </div>
                                                <h3 className="text-xl font-medium mb-2 group-hover:text-[#d97757] transition-colors">{app.name}</h3>
                                                <p className="text-[#999] text-sm leading-relaxed mb-6">{app.description}</p>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <button className="bg-[#f0f0f0] text-[#111] px-4 py-1.5 rounded text-sm font-medium hover:bg-white transition-colors flex items-center gap-2">
                                                    <Download size={14} /> Installieren
                                                </button>
                                            </div>
                                        </div>
                                        {/* Subtle Background Accent */}
                                        <div className="absolute -right-4 -bottom-4 opacity-5 group-hover:opacity-10 transition-opacity">
                                            <Github size={120} />
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </section>
                    )}

                    <section>
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-xl font-light tracking-tight text-white">
                                {activeTab === "Alle" ? "Alle Entdeckungen" : activeTab}
                            </h2>
                            <span className="text-xs text-[#666] font-mono">{filteredApps.length} Ergebnisse</span>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            {filteredApps.map(app => (
                                <div
                                    key={app.id}
                                    onClick={() => setSelectedApp(app)}
                                    className="bg-[#161616] border border-[#2a2a2a] p-5 rounded-lg hover:bg-[#1a1a1a] transition-all cursor-pointer group"
                                >
                                    <div className="flex justify-between items-start mb-4">
                                        <div className="w-10 h-10 bg-[#222] rounded flex items-center justify-center group-hover:bg-[#2a2a2a] transition-colors">
                                            {app.category === "AI" ? <Cpu size={20} className="text-[#d97757]" /> : <Code size={20} className="text-[#999]" />}
                                        </div>
                                        <div className="flex flex-col items-end">
                                            <span className="text-[10px] text-[#555] font-mono mb-1">v{app.version}</span>
                                            <div className="flex items-center gap-1 text-[10px] text-[#666]"><Star size={10} /> {app.stars}</div>
                                        </div>
                                    </div>
                                    <h3 className="text-lg font-medium mb-1 group-hover:text-white transition-colors">{app.name}</h3>
                                    <div className="text-xs text-[#666] mb-3">von {app.developer}</div>
                                    <p className="text-[#888] text-xs leading-relaxed line-clamp-2">{app.description}</p>

                                    <div className="mt-4 pt-4 border-t border-[#222] flex items-center justify-between">
                                        <div className="flex gap-1">
                                            {app.tags.map(tag => (
                                                <span key={tag} className="text-[9px] px-1.5 py-0.5 bg-[#222] text-[#666] rounded">{tag}</span>
                                            ))}
                                        </div>
                                        <ChevronRight size={14} className="text-[#333] group-hover:text-[#d97757] transition-all" />
                                    </div>
                                </div>
                            ))}
                        </div>
                    </section>
                </div>

                {/* Modal App Detail */}
                {selectedApp && (
                    <div className="absolute inset-0 z-50 flex items-center justify-center p-8 bg-black/60 backdrop-blur-sm">
                        <div className="bg-[#161616] w-full max-w-3xl border border-[#333] rounded-2xl overflow-hidden shadow-2xl animate-in fade-in zoom-in duration-200">
                            <div className="flex justify-between items-start p-6 border-b border-[#2a2a2a]">
                                <div className="flex gap-6">
                                    <div className="w-20 h-20 bg-[#222] rounded-xl flex items-center justify-center shadow-inner">
                                        <Github size={40} className="text-[#d97757]" />
                                    </div>
                                    <div>
                                        <h2 className="text-3xl font-light mb-1">{selectedApp.name}</h2>
                                        <div className="flex items-center gap-4 text-sm text-[#666]">
                                            <span className="flex items-center gap-1"><Star size={14} className="fill-[#666]" /> {selectedApp.stars}</span>
                                            <span>v{selectedApp.version}</span>
                                            <span className="px-2 py-0.5 bg-[#222] rounded text-xs">{selectedApp.category}</span>
                                        </div>
                                    </div>
                                </div>
                                <button
                                    onClick={() => setSelectedApp(null)}
                                    className="p-2 hover:bg-[#222] rounded-full text-[#666] hover:text-white transition-colors"
                                >
                                    <X size={24} />
                                </button>
                            </div>

                            <div className="p-8 grid grid-cols-3 gap-8">
                                <div className="col-span-2 space-y-6">
                                    <div>
                                        <h3 className="text-sm font-semibold uppercase tracking-widest text-[#666] mb-3">Über diese App</h3>
                                        <p className="text-[#aaa] leading-relaxed italic text-lg">
                                            "{selectedApp.description}"
                                        </p>
                                        <p className="text-[#888] mt-4 leading-relaxed">
                                            Optimiert für Entwickler, die Wert auf Geschwindigkeit und Sicherheit legen. Integriert sich nahtlos in deinen bestehenden Workflow und bietet umfangreiche Anpassungsmöglichkeiten über die `config.yaml`.
                                        </p>
                                    </div>

                                    <div className="grid grid-cols-2 gap-4 pt-4">
                                        <div className="p-4 rounded-lg border border-[#222] bg-[#1a1a1a]">
                                            <h4 className="text-[10px] text-[#555] uppercase font-bold mb-2">Technologie</h4>
                                            <p className="text-sm">Rust / WebAssembly</p>
                                        </div>
                                        <div className="p-4 rounded-lg border border-[#222] bg-[#1a1a1a]">
                                            <h4 className="text-[10px] text-[#555] uppercase font-bold mb-2">Lizenz</h4>
                                            <p className="text-sm">MIT Open Source</p>
                                        </div>
                                    </div>
                                </div>

                                <div className="col-span-1 space-y-4">
                                    <button className="w-full bg-[#d97757] hover:bg-[#e08a6d] text-black font-semibold py-3 rounded-lg transition-all flex items-center justify-center gap-2">
                                        <Download size={18} /> Installieren
                                    </button>
                                    <button className="w-full border border-[#333] hover:bg-[#222] text-[#ccc] py-3 rounded-lg transition-all flex items-center justify-center gap-2">
                                        <ExternalLink size={18} /> GitHub Repo
                                    </button>

                                    <div className="mt-8">
                                        <h3 className="text-xs font-semibold text-[#444] uppercase mb-4">Entwickler</h3>
                                        <div className="flex items-center gap-3">
                                            <div className="w-8 h-8 rounded bg-[#333]" />
                                            <div className="text-sm text-[#999]">{selectedApp.developer}</div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div className="bg-[#0d0d0d] p-6 flex items-center justify-between border-t border-[#2a2a2a]">
                                <div className="flex items-center gap-2 text-[#555] text-xs">
                                    <ShieldCheck size={14} /> Verifiziertes Repository
                                </div>
                                <div className="flex gap-4">
                                    <span className="text-[#555] text-xs">Erstmals veröffentlicht: 2023</span>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </main>

            <style>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: transparent;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: #2a2a2a;
          border-radius: 10px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: #3a3a3a;
        }
        @keyframes fadeIn {
          from { opacity: 0; }
          to { opacity: 1; }
        }
        .animate-in {
          animation: fadeIn 0.2s ease-out;
        }
      `}</style>
        </div>
    );
};

export default App;