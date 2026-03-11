using DrawMap.Domain;
using DomainRoute = DrawMap.Domain.Route;

namespace DrawMap.WebAPI.Services;

public interface IRouteService
{
    Task<DomainRoute> AddRoute(DomainRoute route);
    Task<DomainRoute?> GetRoute(string routeId);
    Task<bool> DeleteRoute(string routeId);
    Task<bool> UpdateRoute(string routeId, DomainRoute route);
    Task<Photo> AddPhoto(string routeId, Photo photo);
}
