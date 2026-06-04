import {
  createVehicle,
  getVehicles,
  getDocuments,
  createDocument
} from "./services/api";
import { useState } from "react";
import { useEffect } from "react";

function VehicleForm() {

  const [plate, setPlate] = useState("");
  const [chassisNumber, setChassisNumber] = useState("");
  const [brand, setBrand] = useState("");
  const [model, setModel] = useState("");
  const [year, setYear] = useState("");
  const [vehicleType, setVehicleType] = useState("");
  const [vehicles, setVehicles] = useState<any[]>([]);

  useEffect(() => {
    loadVehicles();
  }, []);

  const loadVehicles = async () => {
    const data = await getVehicles();
    setVehicles(data);
  };

  const handleSubmit = async (e: any) => {

    e.preventDefault();

    const vehicle = {
      plate,
      chassisNumber,
      brand,
      model,
      year: Number(year),
      vehicleType
    };

    const result = await createVehicle(vehicle);

    await loadVehicles();
    alert(
        `Vehículo registrado: ${result.plate}`
    );

    setPlate("");
    setChassisNumber("");
    setBrand("");
    setModel("");
    setYear("");
    setVehicleType("");
  };

  return (

      <div className="card">

        <div className="card-header">
          Registrar Vehículo
        </div>

        <div className="card-body">

          <form onSubmit={handleSubmit}>

            <input
                className="form-control mb-2"
                placeholder="Placa"
                value={plate}
                onChange={(e) =>
                    setPlate(e.target.value)}
            />

            <input
                className="form-control mb-2"
                placeholder="Chasis"
                value={chassisNumber}
                onChange={(e) =>
                    setChassisNumber(e.target.value)}
            />

            <input
                className="form-control mb-2"
                placeholder="Marca"
                value={brand}
                onChange={(e) =>
                    setBrand(e.target.value)}
            />

            <input
                className="form-control mb-2"
                placeholder="Modelo"
                value={model}
                onChange={(e) =>
                    setModel(e.target.value)}
            />

            <input
                className="form-control mb-2"
                placeholder="Año"
                value={year}
                onChange={(e) =>
                    setYear(e.target.value)}
            />

            <input
                className="form-control mb-3"
                placeholder="Tipo"
                value={vehicleType}
                onChange={(e) =>
                    setVehicleType(e.target.value)}
            />

            <button
                className="btn btn-success">

              Guardar Vehículo

            </button>

          </form>

          <hr />

          <h4>Vehículos Registrados</h4>

          <table className="table table-striped">

            <thead>
            <tr>
              <th>ID</th>
              <th>Placa</th>
              <th>Marca</th>
              <th>Modelo</th>
              <th>Estado</th>
            </tr>
            </thead>

            <tbody>

            {vehicles.map(vehicle => (

                <tr key={vehicle.id}>
                  <td>{vehicle.id}</td>
                  <td>{vehicle.plate}</td>
                  <td>{vehicle.brand}</td>
                  <td>{vehicle.model}</td>
                  <td>{vehicle.status}</td>
                </tr>

            ))}

            </tbody>

          </table>

        </div>

      </div>
  );
}

function DocumentForm() {

  const [documentNumber, setDocumentNumber] = useState("");
  const [documentType, setDocumentType] = useState("SOAT");
  const [issueDate, setIssueDate] = useState("");
  const [expirationDate, setExpirationDate] = useState("");
  const [vehicleId, setVehicleId] = useState("");

  const [documents, setDocuments] = useState<any[]>([]);

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    const data = await getDocuments();
    setDocuments(data);
  };

  const handleSubmit = async (e: any) => {

    e.preventDefault();

    const document = {
      documentNumber,
      documentType,
      issueDate,
      expirationDate,
      vehicleId: Number(vehicleId)
    };

    const result = await createDocument(document);

    await loadDocuments();

    alert(
        `Documento registrado: ${result.documentNumber}`
    );

    setDocumentNumber("");
    setIssueDate("");
    setExpirationDate("");
    setVehicleId("");
  };

  return (

      <div className="card">

        <div className="card-header">
          Registrar Documento
        </div>

        <div className="card-body">

          <form onSubmit={handleSubmit}>

            <input
                className="form-control mb-2"
                placeholder="Número Documento"
                value={documentNumber}
                onChange={(e) =>
                    setDocumentNumber(e.target.value)}
            />

            <select
                className="form-control mb-2"
                value={documentType}
                onChange={(e) =>
                    setDocumentType(e.target.value)}
            >
              <option value="SOAT">SOAT</option>
              <option value="TECHNICAL_REVIEW">
                TECHNICAL_REVIEW
              </option>
            </select>

            <input
                type="date"
                className="form-control mb-2"
                value={issueDate}
                onChange={(e) =>
                    setIssueDate(e.target.value)}
            />

            <input
                type="date"
                className="form-control mb-2"
                value={expirationDate}
                onChange={(e) =>
                    setExpirationDate(e.target.value)}
            />

            <input
                className="form-control mb-3"
                placeholder="Vehicle ID"
                value={vehicleId}
                onChange={(e) =>
                    setVehicleId(e.target.value)}
            />

            <button className="btn btn-success">
              Guardar Documento
            </button>

          </form>

          <hr />

          <h4>Documentos Registrados</h4>

          <table className="table table-striped">

            <thead>
            <tr>
              <th>ID</th>
              <th>Número</th>
              <th>Tipo</th>
              <th>Vencimiento</th>
              <th>Vehículo</th>
            </tr>
            </thead>

            <tbody>

            {documents.map(doc => (

                <tr key={doc.id}>
                  <td>{doc.id}</td>
                  <td>{doc.documentNumber}</td>
                  <td>{doc.documentType}</td>
                  <td>{doc.expirationDate}</td>
                  <td>{doc.vehicleId}</td>
                </tr>

            ))}

            </tbody>

          </table>

        </div>

      </div>
  );
}

function App() {

  const [view, setView] = useState("dashboard");

  return (
      <div className="container mt-4">

        <h1>🚚 Fleet Service Dashboard</h1>

        <hr />

        <div className="btn-group mb-4">

          <button
              className="btn btn-primary"
              onClick={() => setView("dashboard")}
          >
            Dashboard
          </button>

          <button
              className="btn btn-secondary"
              onClick={() => setView("vehicles")}
          >
            Vehículos
          </button>

          <button
              className="btn btn-secondary"
              onClick={() => setView("documents")}
          >
            Documentos
          </button>

          <button
              className="btn btn-secondary"
              onClick={() => setView("locations")}
          >
            Ubicaciones
          </button>

          <button
              className="btn btn-secondary"
              onClick={() => setView("assignments")}
          >
            Asignaciones
          </button>

        </div>

        {view === "dashboard" && (
            <div className="alert alert-success">
              Bienvenido al Dashboard
            </div>
        )}

        {view === "vehicles" && (
            <VehicleForm />
        )}

        {view === "documents" && (
            <DocumentForm />
        )}

        {view === "locations" && (
            <div className="card">
              <div className="card-body">
                Módulo Ubicaciones
              </div>
            </div>
        )}

        {view === "assignments" && (
            <div className="card">
              <div className="card-body">
                Módulo Asignaciones

              </div>
            </div>
        )}

      </div>
  );
}

export default App;