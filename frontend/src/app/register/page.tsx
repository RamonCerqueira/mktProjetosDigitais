"use client";
import api from "@/lib/api";
import { CepLookup, CnpjLookup, DocumentType, DocumentValidation, ReverseGeocode } from "@/types";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

const onlyDigits = (value: string) => value.replace(/\D/g, "");

export default function RegisterPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    role: "SELLER",
    documentType: "CPF" as DocumentType,
    documentNumber: "",
    postalCode: "",
    street: "",
    streetNumber: "",
    complement: "",
    neighborhood: "",
    city: "",
    state: "",
    companyName: "",
    latitude: undefined as number | undefined,
    longitude: undefined as number | undefined,
  });
  const [error, setError] = useState("");
  const [documentStatus, setDocumentStatus] = useState<string>("");
  const [loadingCep, setLoadingCep] = useState(false);
  const [loadingCnpj, setLoadingCnpj] = useState(false);
  const [loadingGeo, setLoadingGeo] = useState(false);

  const normalizedDocument = useMemo(() => onlyDigits(form.documentNumber), [form.documentNumber]);
  const normalizedCep = useMemo(() => onlyDigits(form.postalCode), [form.postalCode]);

  useEffect(() => {
    const requiredLength = form.documentType === "CPF" ? 11 : 14;
    if (normalizedDocument.length !== requiredLength) {
      setDocumentStatus("");
      return;
    }
    const timeout = setTimeout(async () => {
      try {
        const { data } = await api.get<DocumentValidation>(`/integrations/validate/${form.documentType}/${normalizedDocument}`);
        setDocumentStatus(data.valid ? `${form.documentType} válido` : `${form.documentType} inválido`);
        if (form.documentType === "CNPJ" && data.valid) {
          setLoadingCnpj(true);
          const company = (await api.get<CnpjLookup>(`/integrations/cnpj/${normalizedDocument}`)).data;
          setForm((current) => ({
            ...current,
            companyName: company.tradeName || company.companyName || current.companyName,
            street: company.street || current.street,
            neighborhood: company.neighborhood || current.neighborhood,
            city: company.city || current.city,
            state: company.state || current.state,
            postalCode: company.postalCode || current.postalCode,
            email: current.email || company.email || current.email,
          }));
        }
      } catch {
        setDocumentStatus(`${form.documentType} inválido`);
      } finally {
        setLoadingCnpj(false);
      }
    }, 400);
    return () => clearTimeout(timeout);
  }, [form.documentType, normalizedDocument]);

  useEffect(() => {
    if (normalizedCep.length !== 8) return;
    const timeout = setTimeout(async () => {
      try {
        setLoadingCep(true);
        const { data } = await api.get<CepLookup>(`/integrations/cep/${normalizedCep}`);
        setForm((current) => ({
          ...current,
          postalCode: onlyDigits(data.cep),
          street: data.street || current.street,
          complement: data.complement || current.complement,
          neighborhood: data.neighborhood || current.neighborhood,
          city: data.city || current.city,
          state: data.state || current.state,
        }));
      } catch {
        setError("Não foi possível buscar o endereço pelo CEP informado.");
      } finally {
        setLoadingCep(false);
      }
    }, 400);
    return () => clearTimeout(timeout);
  }, [normalizedCep]);

  const requestLocation = () => {
    if (!navigator.geolocation) {
      setError("Seu navegador não suporta geolocalização.");
      return;
    }
    setLoadingGeo(true);
    navigator.geolocation.getCurrentPosition(async ({ coords }) => {
      try {
        const { data } = await api.get<ReverseGeocode>("/integrations/maps/reverse", { params: { lat: coords.latitude, lng: coords.longitude } });
        setForm((current) => ({
          ...current,
          latitude: coords.latitude,
          longitude: coords.longitude,
          city: data.city || current.city,
          state: data.state || current.state,
        }));
      } catch {
        setError("Não foi possível converter sua localização em cidade/estado.");
      } finally {
        setLoadingGeo(false);
      }
    }, () => {
      setLoadingGeo(false);
      setError("Permissão de localização negada.");
    });
  };

  const submit = async () => {
    try {
      setError("");
      const payload = { ...form, documentNumber: normalizedDocument, postalCode: normalizedCep };
      const { data } = await api.post("/auth/register", payload);
      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("refreshToken", data.refreshToken);
      localStorage.setItem("userRole", data.user.role);
      localStorage.setItem("userId", String(data.user.id));
      router.push(data.user.role === "ADMIN" ? "/admin" : "/dashboard");
    } catch (e: any) {
      setError(e?.response?.data?.error || "Falha no cadastro");
    }
  };

  return (
    <main className="mx-auto max-w-2xl px-6 py-16">
      <div className="card space-y-4">
        <div className="flex items-center justify-between gap-4">
          <h1 className="text-3xl font-bold">Criar conta</h1>
          <button className="btn-secondary" type="button" onClick={requestLocation}>{loadingGeo ? "Localizando..." : "Usar minha localização"}</button>
        </div>
        <div className="grid gap-4 md:grid-cols-2">
          <input className="input" placeholder="Nome" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <input className="input" placeholder="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          <input className="input" type="password" placeholder="Senha" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          <select className="input" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}><option value="SELLER">Seller</option><option value="BUYER">Buyer</option></select>
          <select className="input" value={form.documentType} onChange={(e) => setForm({ ...form, documentType: e.target.value as DocumentType, documentNumber: "" })}><option value="CPF">CPF</option><option value="CNPJ">CNPJ</option></select>
          <div>
            <input className="input" placeholder="CPF ou CNPJ" value={form.documentNumber} onChange={(e) => setForm({ ...form, documentNumber: e.target.value })} />
            {documentStatus && <p className={`mt-2 text-sm ${documentStatus.includes("válido") ? "text-emerald-400" : "text-red-400"}`}>{documentStatus}{loadingCnpj ? " · consultando ReceitaWS..." : ""}</p>}
          </div>
          <input className="input md:col-span-2" placeholder="Nome da empresa (auto por CNPJ)" value={form.companyName} onChange={(e) => setForm({ ...form, companyName: e.target.value })} />
          <div>
            <input className="input" placeholder="CEP" value={form.postalCode} onChange={(e) => setForm({ ...form, postalCode: e.target.value })} />
            {loadingCep && <p className="mt-2 text-sm text-slate-400">Consultando ViaCEP...</p>}
          </div>
          <input className="input" placeholder="Rua" value={form.street} onChange={(e) => setForm({ ...form, street: e.target.value })} />
          <input className="input" placeholder="Número" value={form.streetNumber} onChange={(e) => setForm({ ...form, streetNumber: e.target.value })} />
          <input className="input" placeholder="Complemento" value={form.complement} onChange={(e) => setForm({ ...form, complement: e.target.value })} />
          <input className="input" placeholder="Bairro" value={form.neighborhood} onChange={(e) => setForm({ ...form, neighborhood: e.target.value })} />
          <input className="input" placeholder="Cidade" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} />
          <input className="input" placeholder="UF" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value.toUpperCase() })} />
        </div>
        {error && <p className="text-red-400">{error}</p>}
        <button className="btn-primary w-full" onClick={submit}>Cadastrar</button>
      </div>
    </main>
  );
}
