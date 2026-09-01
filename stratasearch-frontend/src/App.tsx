import { BrowserRouter, Route, Routes } from "react-router-dom";
import WorkspacePage from "./pages/WorkspacePage";
import FoldcraftHero from "./pages/FoldcraftHero";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<FoldcraftHero />} />
        <Route path="/lab" element={<WorkspacePage />} />
        <Route path="*" element={<FoldcraftHero />} />
      </Routes>
    </BrowserRouter>
  );
}
