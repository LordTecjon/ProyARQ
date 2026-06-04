const API_URL = "http://localhost:8081/api";

export async function getVehicles() {
    const response = await fetch(`${API_URL}/vehicles`);
    return response.json();
}

export async function createVehicle(vehicle: any) {

    const response = await fetch(
        `${API_URL}/vehicles`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(vehicle)
        }
    );

    return response.json();


}

export async function getDocuments() {

    const response = await fetch(
        `${API_URL}/documents`
    );

    return response.json();
}

export async function createDocument(document: any) {

    const response = await fetch(
        `${API_URL}/documents`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(document)
        }
    );

    return response.json();
}